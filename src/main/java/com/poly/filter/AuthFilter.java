package com.poly.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter({"/user/*", "/admin/*"}) // Chặn các đường dẫn nhạy cảm
public class AuthFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        // Kiểm tra xem trong session có user chưa
        if (req.getSession().getAttribute("currentUser") == null) {
            // Chưa đăng nhập thì đá về trang login (giả sử bạn có trang login.jsp)
            resp.sendRedirect(req.getContextPath() + "/login.jsp?error=PleaseLogin");
        } else {
            chain.doFilter(request, response); // Cho qua
        }
    }
}