<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <title>Login System</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body { background-color: #1a202c; color: white; height: 100vh; display: flex; align-items: center; justify-content: center; }
        .card { background-color: #2d3748; border: 1px solid #4a5568; width: 400px; }
    </style>
</head>
<body>
<div class="card p-4 shadow-lg">
    <h3 class="text-center mb-4">Welcome Back</h3>

    <%-- Hiển thị thông báo lỗi nếu có --%>
    <c:if test="${not empty param.message}">
        <div class="alert alert-danger">${param.message}</div>
    </c:if>

    <c:if test="${not empty param.error}">
        <div class="alert alert-warning">Vui lòng đăng nhập trước!</div>
    </c:if>

    <%-- Form đăng nhập thường (Login bằng ID trong DB) --%>
    <form action="${pageContext.request.contextPath}/login" method="post">
        <div class="mb-3">
            <label>Username (ID)</label>
            <input type="text" name="id" class="form-control" placeholder="Enter your ID">
        </div>
        <div class="mb-3">
            <label>Password</label>
            <input type="password" name="password" class="form-control" placeholder="Enter password">
        </div>
        <button class="btn btn-primary w-100">Sign In</button>
    </form>

    <hr class="border-secondary my-4">

    <%-- Nút Login Google (Copy logic URL từ file user.jsp cũ sang đây) --%>
    <c:url var="googleLoginUrl" value="https://accounts.google.com/o/oauth2/auth">
        <c:param name="client_id" value="266380978432-r2o4brm11bgervb21jlhn9s3rbu7k1qf.apps.googleusercontent.com"/>
        <c:param name="redirect_uri" value="http://localhost:8080/login-google"/>
        <c:param name="response_type" value="code"/>
        <c:param name="scope" value="email profile"/>
        <c:param name="approval_prompt" value="force"/>
    </c:url>

    <a href="${googleLoginUrl}" class="btn btn-danger w-100">
        <i class="bi bi-google"></i> Login with Google
    </a>
</div>
</body>
</html>