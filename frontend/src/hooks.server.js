import { redirect } from '@sveltejs/kit';
import axios from 'axios';

export async function handle({ event, resolve }) {
    // Get JWT token and user info from cookies
    const jwt_token = event.cookies.get('jwt_token');
    const userInfoCookie = event.cookies.get('user_info');

    // Add the token to the event locals so it's available in load functions
    event.locals.jwt_token = jwt_token;

    // Get user info from cookie
    if (userInfoCookie) {
        try {
            event.locals.user = JSON.parse(decodeURIComponent(userInfoCookie));
        } catch (error) {
            console.error('Failed to parse user info cookie:', error);
            event.locals.user = {};
        }
    } else {
        event.locals.user = {};
    }

    if (jwt_token && event.locals.user && event.locals.user.name) {
        event.locals.isAuthenticated = true;
    } else {
        event.locals.isAuthenticated = false;
    }

    if (event.request.method === 'OPTIONS') {
        return new Response(null, {
            status: 200,
            headers: {
                'Access-Control-Allow-Origin': '*',
                'Access-Control-Allow-Methods': 'GET, POST, PUT, DELETE, PATCH, OPTIONS',
                'Access-Control-Allow-Headers': 'Content-Type, Authorization'
            }
        });
    }

    return resolve(event);
}