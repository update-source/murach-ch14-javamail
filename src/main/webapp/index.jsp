<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/includes/header.jsp" %>

<section class="card">
    <h1>Join our email list</h1>
    <p>To join our email list, enter your name and email address below.
       We'll send you a welcome email using the JavaMail API.</p>

    <c:if test="${not empty errors}">
        <ul class="alert error">
            <c:forEach var="e" items="${errors}">
                <li><c:out value="${e}"/></li>
            </c:forEach>
        </ul>
    </c:if>

    <form action="<c:url value='/emailList'/>" method="post" class="form">
        <input type="hidden" name="action" value="add">

        <label for="firstName">First Name</label>
        <input id="firstName" type="text" name="firstName" maxlength="50" required
               value="<c:out value='${user.firstName}'/>">

        <label for="lastName">Last Name</label>
        <input id="lastName" type="text" name="lastName" maxlength="50" required
               value="<c:out value='${user.lastName}'/>">

        <label for="email">Email</label>
        <input id="email" type="email" name="email" maxlength="254" required
               value="<c:out value='${user.email}'/>">

        <span class="label">Email format</span>
        <div class="radios">
            <label><input type="radio" name="format" value="text"
                <c:if test="${format ne 'html'}">checked</c:if>> Plain text (text/plain)</label>
            <label><input type="radio" name="format" value="html"
                <c:if test="${format eq 'html'}">checked</c:if>> HTML (text/html)</label>
        </div>

        <button type="submit">Join Now</button>
    </form>
</section>

<section class="card muted">
    <h2>How it works</h2>
    <ol>
        <li><code>EmailListServlet</code> validates the form and stores a <code>User</code> in <code>UserDB</code>.</li>
        <li>It builds a welcome message with <code>MimeMessage</code> + <code>InternetAddress</code>.</li>
        <li>It sends the message with <code>MailUtilLocal</code>, <code>MailUtilGmail</code>
            or the Resend HTTP API, depending on <code>MAIL_MODE</code>.</li>
        <li>If sending fails, the <code>MessagingException</code> is logged and shown on the next page.</li>
    </ol>
    <p><a class="button-link" href="<c:url value='/mail'/>">&#9993; Mở trang Mail Services &rarr;</a>
       &nbsp;xem trạng thái các dịch vụ gửi mail, kiểm tra kết nối, soạn email và lịch sử gửi.</p>
</section>

<%@ include file="/WEB-INF/includes/footer.jsp" %>
