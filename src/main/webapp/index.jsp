<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%
    // Lấy đường dẫn gốc của ứng dụng (Context Path)
    String path = request.getContextPath();
    // Chuyển hướng sang Controller: UserServlet
    response.sendRedirect(path + "/user/index");
%>