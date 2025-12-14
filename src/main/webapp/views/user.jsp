<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <title>User Management</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body { background-color: #1a202c; color: white; }
        .card { background-color: #2d3748; border: 1px solid white; }
        .form-control { background-color: white; color: black; }
    </style>
</head>
<body>
<div class="container mt-5">
    <div class="row justify-content-center">
        <div class="col-md-8">
            <div class="card p-4 mb-4">
                <h3 class="text-white">User Management - CRUD</h3>
                <p class="text-secondary">Fill in the data below.</p>

                <c:if test="${not empty message}">
                    <div class="alert alert-warning">${message}</div>
                </c:if>

                <form action="${pageContext.request.contextPath}/user/index" method="post">
                    <div class="mb-3">
                        <input type="text" name="id" class="form-control" placeholder="UserID" value="${user.id}" ${not empty user.id ? 'readonly' : ''}>
                    </div>
                    <div class="mb-3">
                        <input type="password" name="password" class="form-control" placeholder="Password" value="${user.password}">
                    </div>
                    <div class="mb-3">
                        <input type="text" name="fullname" class="form-control" placeholder="Full-name" value="${user.fullname}">
                    </div>
                    <div class="mb-3">
                        <input type="email" name="email" class="form-control" placeholder="E-mail Address" value="${user.email}">
                    </div>

                    <div class="mb-3">
                        <label>Role:</label>
                        <div class="form-check form-check-inline">
                            <input class="form-check-input" type="radio" name="role" value="true" ${user.admin ? 'checked' : ''}>
                            <label class="form-check-label">Admin</label>
                        </div>
                        <div class="form-check form-check-inline">
                            <input class="form-check-input" type="radio" name="role" value="false" ${!user.admin ? 'checked' : ''}>
                            <label class="form-check-label">User</label>
                        </div>
                    </div>

                    <button formaction="${pageContext.request.contextPath}/user/create" class="btn btn-primary">Create</button>
                    <button formaction="${pageContext.request.contextPath}/user/update" class="btn btn-warning text-dark">Update</button>
                    <button formaction="${pageContext.request.contextPath}/user/delete" class="btn btn-danger">Delete</button>
                    <button formaction="${pageContext.request.contextPath}/user/reset" class="btn btn-success">Reset</button>
                </form>

                <form action="${pageContext.request.contextPath}/user/search" method="post" class="mt-3">
                    <div class="input-group">
                        <input type="text" name="keyword" class="form-control" placeholder="Search by name...">
                        <button class="btn btn-outline-light">Search</button>
                    </div>
                </form>
            </div>

            <table class="table table-light table-striped">
                <thead>
                <tr>
                    <th>User ID</th>
                    <th>Password</th>
                    <th>Full-name</th>
                    <th>Email</th>
                    <th>Role</th>
                    <th>Edit</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="u" items="${items}">
                    <tr>
                        <td>${u.id}</td>
                        <td>${u.password}</td>
                        <td>${u.fullname}</td>
                        <td>${u.email}</td>
                        <td>${u.admin ? 'Admin' : 'User'}</td>
                        <td>
                            <a href="${pageContext.request.contextPath}/user/edit?id=${u.id}">Edit</a> |
                            <a href="${pageContext.request.contextPath}/user/delete?id=${u.id}" onclick="return confirm('Xóa hả?')">Remove</a>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>

            <c:url var="googleLoginUrl" value="https://accounts.google.com/o/oauth2/auth">
                <c:param name="client_id" value="266380978432-r2o4brm11bgervb21jlhn9s3rbu7k1qf.apps.googleusercontent.com"/>
                <c:param name="redirect_uri" value="http://localhost:8080/login-google"/>
                <c:param name="response_type" value="code"/>
                <c:param name="scope" value="email profile"/>
                <c:param name="approval_prompt" value="force"/>
            </c:url>

            <div class="text-center mt-3">
                <a href="${googleLoginUrl}" class="btn btn-danger">
                    <i class="bi bi-google"></i> Login with Google
                </a>
            </div>
        </div>
    </div>
</div>
</body>
</html>