import axios from "axios";
import "dotenv/config";

const AUTH0_DOMAIN = process.env.AUTH0_DOMAIN;
const AUTH0_CLIENT_ID = process.env.AUTH0_CLIENT_ID;

const CONNECTION = "Username-Password-Authentication";

function decodeJwtPayload(token) {
  const payload = token.split(".")[1];
  const decodedPayload = Buffer.from(payload, "base64url").toString("utf-8");
  return JSON.parse(decodedPayload);
}

async function signup(
  email,
  password,
  firstName = null,
  lastName = null,
  cookies
) {
  var options = {
    method: "post",
    url: `https://${AUTH0_DOMAIN}/dbconnections/signup`,
    data: {
      client_id: AUTH0_CLIENT_ID,
      email: email,
      password: password,
      connection: CONNECTION,
    },
  };

  if (firstName && firstName.length > 0) {
    options.data.given_name = firstName;
  }

  if (lastName && lastName.length > 0) {
    options.data.family_name = lastName;
  }

  try {
    await axios(options);

    // Wait briefly so Auth0 role assignment/actions can be reflected before first login.
    await new Promise((resolve) => setTimeout(resolve, 2000));

    return await login(email, password, cookies);
  } catch (error) {
    throw error;
  }
}

async function login(username, password, cookies) {
  var options = {
    method: "post",
    url: `https://${AUTH0_DOMAIN}/oauth/token`,
    data: {
      grant_type: "password",
      username: username,
      password: password,
      audience: `https://${AUTH0_DOMAIN}/api/v2/`,
      scope: "openid profile email",
      client_id: AUTH0_CLIENT_ID,
    },
  };

  try {
    const response = await axios(options);
    const { id_token, access_token } = response.data;

    // Debug output for development
    console.log(id_token);

    const userInfo = await getUserInfo(access_token);
    const decodedToken = decodeJwtPayload(id_token);

    const user = {
      ...userInfo,
      user_roles: decodedToken.user_roles || [],
    };

    cookies.set("jwt_token", id_token, {
      path: "/",
      maxAge: 60 * 60 * 24 * 7,
      sameSite: "lax",
      httpOnly: true,
      secure: process.env.NODE_ENV === "production",
    });

    cookies.set("user_info", JSON.stringify(user), {
      path: "/",
      maxAge: 60 * 60 * 24 * 7,
      sameSite: "lax",
      httpOnly: true,
      secure: process.env.NODE_ENV === "production",
    });

    return { success: true };
  } catch (error) {
    throw error;
  }
}

async function getUserInfo(access_token) {
  var options = {
    method: "get",
    url: `https://${AUTH0_DOMAIN}/oauth/userinfo`,
    headers: {
      "Content-Type": "application/json",
      Authorization: "Bearer " + access_token,
    },
  };

  try {
    const response = await axios(options);
    return response.data;
  } catch (error) {
    throw error;
  }
}

const auth = {
  signup,
  login,
};

export default auth;