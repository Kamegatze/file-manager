async function authorizationChecker(r) {
    let accessControlRequestMethod = r.headersIn['Access-Control-Request-Method'];
    ngx.log(ngx.INFO, `Execute request method is ${r.method}`);
    if (accessControlRequestMethod === undefined || accessControlRequestMethod === null) {
        const cookie = r.headersIn["Cookie"];
        ngx.log(ngx.INFO, `Execute check authentication user with cookies is ${cookie}`);
        const response = await r.subrequest("/api/v1/auth/is-authentication");
        r.return(response.status, response.responseText);
    } else {
        r.return(200);
    }
}

export default {authorizationChecker};