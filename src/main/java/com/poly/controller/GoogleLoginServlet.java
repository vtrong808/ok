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

// Servlet này hứng request trả về từ Google sau khi người dùng chọn tài khoản
@WebServlet("/login-google")
public class GoogleLoginServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Google trả về một mã "code" trên URL
        String code = req.getParameter("code");

        // Nếu không có code -> Người dùng chưa đồng ý hoặc lỗi
        if (code == null || code.isEmpty()) {
            resp.sendRedirect("login.jsp?message=Google Login Failed");
            return;
        }

        try {
            // Bước 1: Dùng "code" đổi lấy "AccessToken"
            String accessToken = GoogleUtils.getToken(code);

            // Bước 2: Dùng "AccessToken" để lấy thông tin User (email, tên, avatar...)
            UserGoogleDto googleUser = GoogleUtils.getUserInfo(accessToken);

            // Bước 3: Kiểm tra xem User này đã có trong Database chưa?
            try (Session session = HibernateUtils.getSessionFactory().openSession()) {
                Transaction tx = session.beginTransaction();

                // Tìm trong DB xem có ai trùng Email không
                User user = session.createQuery("FROM User WHERE email = :email", User.class)
                        .setParameter("email", googleUser.getEmail())
                        .uniqueResult();

                // Bước 4: Nếu chưa có -> Tự động Đăng ký (Lưu mới vào DB)
                if (user == null) {
                    user = new User();
                    user.setId(googleUser.getId()); // Lấy ID của Google làm ID đăng nhập
                    user.setEmail(googleUser.getEmail());
                    user.setFullname(googleUser.getName()); // Lấy tên từ Google
                    user.setPassword("google_pass"); // Mật khẩu ngẫu nhiên (vì login bằng Google ko cần pass)
                    user.setAdmin(false); // Mặc định là user thường

                    session.persist(user); // Lưu vào DB
                }

                tx.commit();

                // Bước 5: Lưu thông tin vào Session (Đánh dấu đã đăng nhập thành công)
                HttpSession httpSession = req.getSession();
                httpSession.setAttribute("currentUser", user);

                // Bước 6: Chuyển hướng về trang chủ quản lý
                resp.sendRedirect(req.getContextPath() + "/user/index");
            }

        } catch (Exception e) {
            e.printStackTrace();
            resp.sendRedirect("login.jsp?message=Login Error: " + e.getMessage());
        }
    }
}