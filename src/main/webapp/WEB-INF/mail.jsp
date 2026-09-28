<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/includes/header.jsp" %>

<section class="card">
    <h1>&#9993; Mail Services</h1>
    <p>Các dịch vụ gửi email của ứng dụng. Dịch vụ mặc định (dùng cho email chào mừng) được chọn bằng
       biến môi trường <code>MAIL_MODE</code> &mdash; hiện tại: <b><code><c:out value="${activeMode}"/></code></b>,
       người gửi: <code><c:out value="${mailFrom}"/></code>.</p>

    <c:if test="${not empty errors}">
        <ul class="alert error">
            <c:forEach var="e" items="${errors}"><li><c:out value="${e}"/></li></c:forEach>
        </ul>
    </c:if>
    <c:if test="${not empty message}">
        <p class="alert ok"><c:out value="${message}"/></p>
    </c:if>

    <div class="services">
        <c:forEach var="s" items="${services}">
            <div class="service ${s.active ? 'active' : ''}">
                <div class="service-head">
                    <b><c:out value="${s.name}"/></b>
                    <c:choose>
                        <c:when test="${s.active}"><span class="badge on">Mặc định</span></c:when>
                        <c:when test="${s.configured}"><span class="badge ready">Đã cấu hình</span></c:when>
                        <c:otherwise><span class="badge off">Chưa cấu hình</span></c:otherwise>
                    </c:choose>
                </div>
                <p><c:out value="${s.description}"/></p>
                <p class="settings"><code><c:out value="${s.settings}"/></code></p>
                <c:if test="${loggedIn and s.configured and s.mode ne 'log'}">
                    <form action="<c:url value='/mail'/>" method="post">
                        <input type="hidden" name="action" value="test">
                        <input type="hidden" name="mode" value="${s.mode}">
                        <input type="hidden" name="csrf" value="<c:out value='${sessionScope.csrfToken}'/>">
                        <button type="submit" class="small">Kiểm tra kết nối</button>
                    </form>
                </c:if>
            </div>
        </c:forEach>
    </div>
</section>

<c:choose>
    <c:when test="${not adminEnabled}">
        <section class="card">
            <p class="alert info">Soạn &amp; gửi email cần quyền admin. Đặt biến môi trường
                <code>ADMIN_PASSWORD</code> để bật chức năng này.</p>
        </section>
    </c:when>
    <c:when test="${not loggedIn}">
        <section class="card">
            <h2>Đăng nhập admin để soạn email</h2>
            <form action="<c:url value='/mail'/>" method="post" class="form">
                <input type="hidden" name="action" value="login">
                <label for="password">Admin password</label>
                <input id="password" type="password" name="password" required autocomplete="current-password">
                <button type="submit">Đăng nhập</button>
            </form>
        </section>
    </c:when>
    <c:otherwise>
        <section class="card">
            <h2>Soạn email</h2>
            <form action="<c:url value='/mail'/>" method="post" class="form">
                <input type="hidden" name="action" value="send">
                <input type="hidden" name="csrf" value="<c:out value='${sessionScope.csrfToken}'/>">

                <label for="mode">Gửi qua</label>
                <select id="mode" name="mode">
                    <c:forEach var="s" items="${services}">
                        <c:if test="${s.configured}">
                            <c:set var="selectedMode" value="${empty param.mode ? activeMode : param.mode}"/>
                            <option value="${s.mode}" ${s.mode eq selectedMode ? 'selected' : ''}>
                                <c:out value="${s.name}"/></option>
                        </c:if>
                    </c:forEach>
                </select>

                <label for="to">To</label>
                <input id="to" type="text" name="to" required placeholder="a@example.com, b@example.com"
                       value="<c:out value='${sentOk ? "" : param.to}'/>">
                <label for="cc">CC</label>
                <input id="cc" type="text" name="cc" placeholder="(tuỳ chọn)"
                       value="<c:out value='${sentOk ? "" : param.cc}'/>">
                <label for="bcc">BCC</label>
                <input id="bcc" type="text" name="bcc" placeholder="(tuỳ chọn)"
                       value="<c:out value='${sentOk ? "" : param.bcc}'/>">
                <label for="subject">Subject</label>
                <input id="subject" type="text" name="subject" maxlength="200" required
                       value="<c:out value='${sentOk ? "" : param.subject}'/>">
                <label for="body">Nội dung</label>
                <textarea id="body" name="body" rows="8" maxlength="20000" required><c:out value="${sentOk ? '' : param.body}"/></textarea>
                <span class="label">Định dạng</span>
                <div class="radios">
                    <label><input type="radio" name="format" value="text"
                        ${param.format ne 'html' ? 'checked' : ''}> text/plain</label>
                    <label><input type="radio" name="format" value="html"
                        ${param.format eq 'html' ? 'checked' : ''}> text/html</label>
                </div>
                <button type="submit">Gửi email</button>
            </form>
            <p class="hint">Nhiều địa chỉ phân cách bằng dấu phẩy. Tối đa 50 người nhận / email.</p>
        </section>

        <section class="card">
            <h2>Lịch sử gửi (50 lần gần nhất)</h2>
            <c:choose>
                <c:when test="${empty history}">
                    <p>Chưa có email nào được gửi kể từ khi server khởi động.</p>
                </c:when>
                <c:otherwise>
                    <div class="table-scroll">
                    <table class="info history">
                        <tr><th>Thời gian</th><th>Dịch vụ</th><th>To</th><th>Subject</th><th>Trạng thái</th></tr>
                        <c:forEach var="h" items="${history}">
                            <tr>
                                <td><c:out value="${h.time}"/></td>
                                <td><code><c:out value="${h.mode}"/></code></td>
                                <td><c:out value="${h.to}"/>
                                    <c:if test="${h.recipientCount > 1}"><small>(${h.recipientCount} người nhận)</small></c:if></td>
                                <td><c:out value="${h.subject}"/></td>
                                <td><span class="badge status-${h.status}"><c:out value="${h.status}"/></span>
                                    <div class="detail"><c:out value="${h.detail}"/></div></td>
                            </tr>
                        </c:forEach>
                    </table>
                    </div>
                </c:otherwise>
            </c:choose>

            <form action="<c:url value='/admin'/>" method="post">
                <input type="hidden" name="action" value="logout">
                <input type="hidden" name="csrf" value="<c:out value='${sessionScope.csrfToken}'/>">
                <button type="submit" class="secondary">Đăng xuất</button>
            </form>
        </section>
    </c:otherwise>
</c:choose>

<%@ include file="/WEB-INF/includes/footer.jsp" %>
