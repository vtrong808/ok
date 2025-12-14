<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%
    String path = request.getContextPath(); // Lấy đường dẫn gốc của ứng dụng (Context Path)
    response.sendRedirect(path + "/user/index"); // Chuyển hướng sang Controller: UserServlet
%>