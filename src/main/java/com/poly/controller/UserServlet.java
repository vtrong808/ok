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
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        String path = req.getServletPath(); // Lấy đường dẫn người dùng đang gọi (ví dụ: /user/index)

        // XỬ LÝ ĐĂNG XUẤT
        if (path.contains("logout")) {
            req.getSession().invalidate(); // Hủy session (xóa sạch thông tin đăng nhập)
            resp.sendRedirect(req.getContextPath() + "/login.jsp"); // Đá về trang login
            return; // Dừng luôn, không chạy code bên dưới nữa
        }

        // XỬ LÝ ĐĂNG NHẬP (USER THƯỜNG)
        if (path.contains("login")) {
            // Nếu là method POST (Người dùng bấm nút Login)
            if (req.getMethod().equalsIgnoreCase("POST")) {
                String id = req.getParameter("id");
                String pass = req.getParameter("password");

                // Mở kết nối Hibernate để kiểm tra DB
                try (Session session = HibernateUtils.getSessionFactory().openSession()) {
                    User user = session.get(User.class, id); // Tìm user theo ID

                    // Kiểm tra: Có user không? Mật khẩu đúng không?
                    if (user != null && user.getPassword().equals(pass)) {
                        // OK -> Lưu user vào Session (để các trang khác biết là đã login)
                        req.getSession().setAttribute("currentUser", user);
                        // Chuyển hướng vào trang quản lý
                        resp.sendRedirect(req.getContextPath() + "/user/index");
                    } else {
                        // Fail -> Báo lỗi và quay lại trang login
                        req.setAttribute("message", "Sai thông tin đăng nhập rồi kìa!!");
                        req.getRequestDispatcher("/login.jsp").forward(req, resp);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    req.setAttribute("message", "Lỗi kết nối CSDL!");
                    req.getRequestDispatcher("/login.jsp").forward(req, resp);
                }
            } else {
                // Nếu là method GET (gõ url /login) -> Chỉ hiện trang login thôi
                resp.sendRedirect(req.getContextPath() + "/login.jsp");
            }
            return; // Dừng xử lý
        }

        // XỬ LÝ CRUD & QUẢN LÝ USER
        try (Session session = HibernateUtils.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction(); // Bắt đầu giao dịch
            try {
                // Điều hướng dựa trên đường dẫn (Routing)
                if (path.contains("create")) {
                    doCreate(req, session); // Gọi hàm thêm mới
                } else if (path.contains("update")) {
                    doUpdate(req, session); // Gọi hàm cập nhật
                } else if (path.contains("delete")) {
                    doDelete(req, session); // Gọi hàm xóa
                } else if (path.contains("edit")) {
                    doEdit(req, session);   // Gọi hàm lấy thông tin lên form
                } else if (path.contains("reset")) {
                    req.setAttribute("user", new User()); // Xóa trắng form
                } else if (path.contains("search")) {
                    doSearch(req, session); // Gọi hàm tìm kiếm
                }

                // Nếu không phải đang tìm kiếm thì load lại toàn bộ danh sách để hiển thị
                if (!path.contains("search")) {
                    List<User> list = session.createQuery("FROM User", User.class).list();
                    req.setAttribute("items", list);
                }

                tx.commit(); // Chốt giao dịch (Lưu thay đổi vào DB)
            } catch (Exception e) {
                tx.rollback(); // Có lỗi thì hoàn tác (Undo)
                req.setAttribute("message", "Lỗi hệ thống: " + e.getMessage());
                e.printStackTrace();
            }
        }

        // Cuối cùng: Chuyển dữ liệu (User, List) sang trang JSP để hiển thị
        req.getRequestDispatcher("/views/user.jsp").forward(req, resp);
    }

    // --- CÁC HÀM PHỤ TRỢ ---

    // Hàm thêm mới User
    private void doCreate(HttpServletRequest req, Session session) {
        User user = new User();
        try {
            readForm(user, req); // Đọc dữ liệu từ form html
            session.persist(user); // Lưu vào DB
            req.setAttribute("message", "Thêm mới thành công!");
        } catch (Exception e) {
            req.setAttribute("message", "Thêm thất bại (Trùng ID chăng?)");
        }
    }

    // Hàm cập nhật User
    private void doUpdate(HttpServletRequest req, Session session) {
        try {
            String id = req.getParameter("id");
            User user = session.get(User.class, id); // Tìm user cũ
            if (user != null) {
                readForm(user, req); // Cập nhật thông tin mới
                session.merge(user); // Update vào DB
                req.setAttribute("message", "Cập nhật thành công!");
            }
        } catch (Exception e) {
            req.setAttribute("message", "Cập nhật thất bại!");
        }
    }

    // Hàm xóa User
    private void doDelete(HttpServletRequest req, Session session) {
        String id = req.getParameter("id");
        User user = session.get(User.class, id);
        if (user != null) {
            session.remove(user); // Xóa khỏi DB
            req.setAttribute("message", "Xóa thành công!");
        }
    }

    // Hàm đẩy thông tin User lên form để sửa
    private void doEdit(HttpServletRequest req, Session session) {
        String id = req.getParameter("id");
        User user = session.get(User.class, id);
        req.setAttribute("user", user);
    }

    // Hàm tìm kiếm theo tên và ID
    private void doSearch(HttpServletRequest req, Session session) {
        String keyword = req.getParameter("keyword");

        // Xử lý nếu keyword null (tránh lỗi)
        if (keyword == null) keyword = "";
        keyword = keyword.trim(); // Trim khoảng trắng thừa 2 đầu
        // HQL: Tìm ID hoặc Fullname chứa từ khóa (dùng wildcard %)
        String hql = "FROM User WHERE id LIKE :kw OR fullname LIKE :kw";
        Query<User> query = session.createQuery(hql, User.class);
        // Gán tham số: thêm dấu % bao quanh để tìm 'gần đúng' (contains)
        query.setParameter("kw", "%" + keyword + "%");
        List<User> list = query.list();

        // Gửi kết quả về JSP
        req.setAttribute("items", list);
        req.setAttribute("searchKeyword", keyword); // Gửi lại từ khóa để hiển thị lại trên ô input (UX tốt hơn)
    }

    // Hàm đọc dữ liệu từ Form HTML đổ vào đối tượng User
    private void readForm(User user, HttpServletRequest req) {
        user.setId(req.getParameter("id"));
        user.setPassword(req.getParameter("password"));
        user.setFullname(req.getParameter("fullname"));
        user.setEmail(req.getParameter("email"));
        // Checkbox/Radio trả về "true" nếu được chọn
        user.setAdmin(req.getParameter("role") != null && req.getParameter("role").equals("true"));
    }
}