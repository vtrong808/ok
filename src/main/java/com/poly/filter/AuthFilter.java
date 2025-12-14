package com.poly.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

// Bộ lọc này sẽ chặn mọi truy cập vào các đường dẫn bắt đầu bằng /user/ hoặc /admin/
@WebFilter({"/user/*", "/admin/*"})
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        // Lấy thông tin người dùng từ Session
        Object user = req.getSession().getAttribute("currentUser");

        // Logic kiểm tra:
        if (user == null) {
            // Nếu chưa đăng nhập -> "Đá" về trang login và báo lỗi
            resp.sendRedirect(req.getContextPath() + "/login.jsp?error=Vui long dang nhap!");
        } else {
            // Nếu đã đăng nhập -> Cho phép đi tiếp (vào Servlet/JSP)
            chain.doFilter(request, response);
        }
    }
}