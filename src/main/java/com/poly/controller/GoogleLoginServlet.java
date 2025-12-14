package com.poly.controller;

import com.poly.dto.UserGoogleDto;
import com.poly.entity.User;
import com.poly.utils.GoogleUtils;
import com.poly.utils.HibernateUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.io.IOException;

@WebServlet("/login-google")
public class GoogleLoginServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String code = req.getParameter("code");

        if (code == null || code.isEmpty()) {
            resp.sendRedirect("login.jsp?message=Google Login Failed");
            return;
        }

        try {
            // 1. Lấy token và thông tin user từ Google
            String accessToken = GoogleUtils.getToken(code);
            UserGoogleDto googleUser = GoogleUtils.getUserInfo(accessToken);

            // 2. Kiểm tra xem email này đã có trong Database chưa
            try (Session session = HibernateUtils.getSessionFactory().openSession()) {
                Transaction tx = session.beginTransaction();

                // Tìm user theo email (giả sử email là unique)
                User user = session.createQuery("FROM User WHERE email = :email", User.class)
                        .setParameter("email", googleUser.getEmail())
                        .uniqueResult();

                // 3. Nếu chưa có -> Tự động Đăng ký (Tạo mới user)
                if (user == null) {
                    user = new User();
                    user.setId(googleUser.getId().substring(0, 20)); // Cắt ngắn ID nếu quá dài
                    user.setEmail(googleUser.getEmail());
                    user.setFullname(googleUser.getName());
                    user.setPassword("google_pass"); // Mật khẩu ngẫu nhiên hoặc placeholder
                    user.setAdmin(false);

                    session.persist(user); // Lưu vào DB
                }

                tx.commit();

                // 4. Lưu thông tin vào Session (Đăng nhập thành công)
                HttpSession httpSession = req.getSession();
                httpSession.setAttribute("currentUser", user);

                // 5. Chuyển hướng về trang chủ
                resp.sendRedirect(req.getContextPath() + "/user/index");
            }

        } catch (Exception e) {
            e.printStackTrace();
            resp.sendRedirect("login.jsp?message=Login Error: " + e.getMessage());
        }
    }
}