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

@WebServlet({"/user/index", "/user/create", "/user/update", "/user/delete", "/user/edit", "/user/reset", "/user/search"})
public class UserServlet extends HttpServlet {

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();

        // Mở session Hibernate (như mở cửa kho)
        try (Session session = HibernateUtils.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                if (path.contains("create")) {
                    doCreate(req, session); // [cite: 6]
                } else if (path.contains("update")) {
                    doUpdate(req, session); // [cite: 8]
                } else if (path.contains("delete")) {
                    doDelete(req, session); // [cite: 7]
                } else if (path.contains("edit")) {
                    doEdit(req, session);   // [cite: 8]
                } else if (path.contains("reset")) {
                    req.setAttribute("user", new User());
                } else if (path.contains("search")) {
                    doSearch(req, session); //
                }

                if(!path.contains("search")) {
                    // Mặc định load lại list user sau khi thao tác
                    List<User> list = session.createQuery("FROM User", User.class).list();
                    req.setAttribute("items", list);
                }

                tx.commit();
            } catch (Exception e) {
                tx.rollback();
                req.setAttribute("message", "Lỗi: " + e.getMessage());
                e.printStackTrace();
            }
        }

        req.getRequestDispatcher("/views/user.jsp").forward(req, resp);
    }

    private void doCreate(HttpServletRequest req, Session session) {
        User user = new User();
        try {
            // Map dữ liệu từ form vào entity (dùng BeanUtils.populate nếu muốn nhanh hơn)
            readForm(user, req);
            session.persist(user);
            req.setAttribute("message", "Thêm mới thành công!");
        } catch (Exception e) {
            req.setAttribute("message", "Thêm mới thất bại (Trùng ID chăng?)");
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
        req.setAttribute("user", user); // Đẩy user lên form để sửa
    }

    //  Chức năng tìm kiếm
    private void doSearch(HttpServletRequest req, Session session) {
        String keyword = req.getParameter("keyword");
        // HQL query tìm theo tên
        String hql = "FROM User WHERE fullname LIKE :kw";
        Query<User> query = session.createQuery(hql, User.class);
        query.setParameter("kw", "%" + keyword + "%");
        List<User> list = query.list();
        req.setAttribute("items", list);
    }

    // Hàm phụ trợ đọc form thủ công (hoặc dùng BeanUtils)
    private void readForm(User user, HttpServletRequest req) {
        user.setId(req.getParameter("id"));
        user.setPassword(req.getParameter("password"));
        user.setFullname(req.getParameter("fullname"));
        user.setEmail(req.getParameter("email"));
        user.setAdmin(req.getParameter("role") != null && req.getParameter("role").equals("true"));
    }
}