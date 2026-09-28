<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/includes/header.jsp" %>

<h1>Mail Services</h1>
<p>Dịch vụ mặc định (MAIL_MODE): <b><c:out value="${activeMode}"/></b> -
   Người gửi: <c:out value="${mailFrom}"/></p>

<c:forEach var="e" items="${errors}">
    <p class="error"><i><c:out value="${e}"/></i></p>
</c:forEach>
<c:if test="${not empty message}">
    <p class="success"><c:out value="${message}"/></p>
</c:if>

<table>
    <tr><th>Dịch vụ</th><th>Mô tả</th><th>Cấu hình</th><th>Trạng thái</th>
        <c:if test="${loggedIn}"><th>&nbsp;</th></c:if></tr>
    <c:forEach var="s" items="${services}">
        <tr>
            <td><c:out value="${s.name}"/></td>
            <td><c:out value="${s.description}"/></td>
            <td><c:out value="${s.settings}"/></td>
            <td>
                <c:choose>
                    <c:when test="${s.active}"><b>Mặc định</b></c:when>
                    <c:when test="${s.configured}">Đã cấu hình</c:when>
                    <c:otherwise>Chưa cấu hình</c:otherwise>
                </c:choose>
            </td>
            <c:if test="${loggedIn}">
                <td>
                    <c:if test="${s.configured and s.mode ne 'log'}">
                        <form action="<c:url value='/mail'/>" method="post">
                            <input type="hidden" name="action" value="test">
                            <input type="hidden" name="mode" value="${s.mode}">
                            <input type="hidden" name="csrf" value="<c:out value='${sessionScope.csrfToken}'/>">
                            <input type="submit" value="Kiểm tra kết nối">
                        </form>
                    </c:if>
                </td>
            </c:if>
        </tr>
    </c:forEach>
</table>

<c:choose>
    <c:when test="${not adminEnabled}">
        <p><i>Soạn và gửi email cần quyền admin. Đặt biến môi trường
           ADMIN_PASSWORD để bật chức năng này.</i></p>
    </c:when>
    <c:when test="${not loggedIn}">
        <h2>Đăng nhập admin để soạn email</h2>
        <form action="<c:url value='/mail'/>" method="post">
            <input type="hidden" name="action" value="login">
            <label class="pad_top">Password:</label>
            <input type="password" name="password" required><br>
            <label>&nbsp;</label>
            <input type="submit" value="Đăng nhập" class="margin_left">
        </form>
    </c:when>
    <c:otherwise>
        <h2>Soạn email</h2>
        <form action="<c:url value='/mail'/>" method="post">
            <input type="hidden" name="action" value="send">
            <input type="hidden" name="csrf" value="<c:out value='${sessionScope.csrfToken}'/>">
            <c:set var="selectedMode" value="${empty param.mode ? activeMode : param.mode}"/>
            <label class="pad_top">Gửi qua:</label>
            <select name="mode">
                <c:forEach var="s" items="${services}">
                    <c:if test="${s.configured}">
                        <option value="${s.mode}" ${s.mode eq selectedMode ? 'selected' : ''}><c:out value="${s.name}"/></option>
                    </c:if>
                </c:forEach>
            </select><br>
            <label class="pad_top">To:</label>
            <input type="text" name="to" required
                   value="<c:out value='${sentOk ? "" : param.to}'/>"><br>
            <label class="pad_top">CC:</label>
            <input type="text" name="cc"
                   value="<c:out value='${sentOk ? "" : param.cc}'/>"><br>
            <label class="pad_top">BCC:</label>
            <input type="text" name="bcc"
                   value="<c:out value='${sentOk ? "" : param.bcc}'/>"><br>
            <label class="pad_top">Subject:</label>
            <input type="text" name="subject" maxlength="200" required
                   value="<c:out value='${sentOk ? "" : param.subject}'/>"><br>
            <label class="pad_top">Nội dung:</label>
            <textarea name="body" rows="8" maxlength="20000" required><c:out value="${sentOk ? '' : param.body}"/></textarea><br>
            <label class="pad_top">Định dạng:</label>
            <span>
                <input type="radio" name="format" value="text" ${param.format ne 'html' ? 'checked' : ''}> text/plain
                <input type="radio" name="format" value="html" ${param.format eq 'html' ? 'checked' : ''}> text/html
            </span><br>
            <label>&nbsp;</label>
            <input type="submit" value="Gửi email" class="margin_left">
        </form>
        <p><i>Nhiều địa chỉ phân cách bằng dấu phẩy. Tối đa 50 người nhận / email.</i></p>

        <h2>Lịch sử gửi (50 lần gần nhất)</h2>
        <c:choose>
            <c:when test="${empty history}">
                <p>Chưa có email nào được gửi kể từ khi server khởi động.</p>
            </c:when>
            <c:otherwise>
                <table>
                    <tr><th>Thời gian</th><th>Dịch vụ</th><th>To</th><th>Số người nhận</th>
                        <th>Subject</th><th>Trạng thái</th><th>Chi tiết</th></tr>
                    <c:forEach var="h" items="${history}">
                        <tr>
                            <td><c:out value="${h.time}"/></td>
                            <td><c:out value="${h.mode}"/></td>
                            <td><c:out value="${h.to}"/></td>
                            <td>${h.recipientCount}</td>
                            <td><c:out value="${h.subject}"/></td>
                            <td><c:out value="${h.status}"/></td>
                            <td><c:out value="${h.detail}"/></td>
                        </tr>
                    </c:forEach>
                </table>
            </c:otherwise>
        </c:choose>

        <form action="<c:url value='/admin'/>" method="post">
            <input type="hidden" name="action" value="logout">
            <input type="hidden" name="csrf" value="<c:out value='${sessionScope.csrfToken}'/>">
            <input type="submit" value="Đăng xuất">
        </form>
    </c:otherwise>
</c:choose>

<%@ include file="/WEB-INF/includes/footer.jsp" %>
