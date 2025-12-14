package com.poly.controller;

import com.poly.entity.User;
import com.poly.utils.HibernateUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.io.IOException;
import java.util.List;

@WebServlet({"/user/index", "/user/create", "/user/update", "/user/delete", "/user/edit", "/user/reset", "/user/search", "/login", "/logout"})
public class UserServlet extends HttpServlet {

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();

        // --- 1. Xử lý Đăng xuất ---
        if (path.contains("logout")) {
            req.getSession().invalidate(); // Hủy toàn bộ session
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
            return; // Dừng, không chạy code bên dưới
        }

        // --- 2. Xử lý Đăng nhập thường (POST từ login.jsp) ---
        if (path.contains("login")) {
            if (req.getMethod().equalsIgnoreCase("POST")) {
                String id = req.getParameter("id");
                String pass = req.getParameter("password");

                try (Session session = HibernateUtils.getSessionFactory().openSession()) {
                    User user = session.get(User.class, id); // Tìm user theo ID

                    if (user != null && user.getPassword().equals(pass)) {
                        // Thành công: Lưu user vào session
                        req.getSession().setAttribute("currentUser", user);
                        // Chuyển hướng vào trang quản lý
                        resp.sendRedirect(req.getContextPath() + "/user/index");
                    } else {
                        // Thất bại: Báo lỗi và quay lại trang login
                        req.setAttribute("message", "Sai thông tin đăng nhập!");
                        req.getRequestDispatcher("/login.jsp").forward(req, resp);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    req.setAttribute("message", "Lỗi xử lý đăng nhập!");
                    req.getRequestDispatcher("/login.jsp").forward(req, resp);
                }
            } else {
                // Nếu người dùng gõ /login trên URL (GET) -> Đẩy về trang jsp
                resp.sendRedirect(req.getContextPath() + "/login.jsp");
            }
            return; // Quan trọng: Dừng xử lý để không chạy xuống phần CRUD
        }

        // --- 3. Xử lý CRUD & Quản lý User (Chỉ chạy khi đã login hoặc tắt Filter) ---
        // Mở session Hibernate
        try (Session session = HibernateUtils.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                if (path.contains("create")) {
                    doCreate(req, session);
                } else if (path.contains("update")) {
                    doUpdate(req, session);
                } else if (path.contains("delete")) {
                    doDelete(req, session);
                } else if (path.contains("edit")) {
                    doEdit(req, session);
                } else if (path.contains("reset")) {
                    req.setAttribute("user", new User());
                } else if (path.contains("search")) {
                    doSearch(req, session);
                }

                // Nếu không phải là search thì load lại toàn bộ danh sách
                if (!path.contains("search")) {
                    List<User> list = session.createQuery("FROM User", User.class).list();
                    req.setAttribute("items", list);
                }

                tx.commit();
            } catch (Exception e) {
                tx.rollback();
                req.setAttribute("message", "Lỗi hệ thống: " + e.getMessage());
                e.printStackTrace();
            }
        }

        // Forward dữ liệu ra trang hiển thị
        req.getRequestDispatcher("/views/user.jsp").forward(req, resp);
    }

    // --- Các hàm phụ trợ CRUD ---

    private void doCreate(HttpServletRequest req, Session session) {
        User user = new User();
        try {
            readForm(user, req);
            session.persist(user);
            req.setAttribute("message", "Thêm mới thành công!");
        } catch (Exception e) {
            req.setAttribute("message", "Thêm mới thất bại (Trùng ID?)");
        }
    }

    private void doUpdate(HttpServletRequest req, Session session) {
        try {
            String id = req.getParameter("id");
            User user = session.get(User.class, id);
            if (user != null) {
                readForm(user, req);
                session.merge(user);
                req.setAttribute("message", "Cập nhật thành công!");
            }
        } catch (Exception e) {
            req.setAttribute("message", "Cập nhật thất bại!");
        }
    }

    private void doDelete(HttpServletRequest req, Session session) {
        String id = req.getParameter("id");
        User user = session.get(User.class, id);
        if (user != null) {
            session.remove(user);
            req.setAttribute("message", "Xóa thành công!");
        }
    }

    private void doEdit(HttpServletRequest req, Session session) {
        String id = req.getParameter("id");
        User user = session.get(User.class, id);
        req.setAttribute("user", user);
    }

    private void doSearch(HttpServletRequest req, Session session) {
        String keyword = req.getParameter("keyword");
        String hql = "FROM User WHERE fullname LIKE :kw";
        Query<User> query = session.createQuery(hql, User.class);
        query.setParameter("kw", "%" + keyword + "%");
        List<User> list = query.list();
        req.setAttribute("items", list);
    }

    private void readForm(User user, HttpServletRequest req) {
        user.setId(req.getParameter("id"));
        user.setPassword(req.getParameter("password"));
        user.setFullname(req.getParameter("fullname"));
        user.setEmail(req.getParameter("email"));
        user.setAdmin(req.getParameter("role") != null && req.getParameter("role").equals("true"));
    }
}