<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/includes/header.jsp" %>

<section class="card">
    <h1>Admin &middot; Send to the whole list</h1>

    <c:if test="${not empty errors}">
        <ul class="alert error">
            <c:forEach var="e" items="${errors}"><li><c:out value="${e}"/></li></c:forEach>
        </ul>
    </c:if>
    <c:if test="${not empty message}">
        <p class="alert ok"><c:out value="${message}"/></p>
    </c:if>

    <c:choose>
        <c:when test="${not adminEnabled}">
            <p class="alert info">The admin page is disabled. Set the <code>ADMIN_PASSWORD</code>
                environment variable to enable it.</p>
        </c:when>
        <c:when test="${not loggedIn}">
            <form action="<c:url value='/admin'/>" method="post" class="form">
                <input type="hidden" name="action" value="login">
                <label for="password">Admin password</label>
                <input id="password" type="password" name="password" required autocomplete="current-password">
                <button type="submit">Log in</button>
            </form>
        </c:when>
        <c:otherwise>
            <p>MAIL_MODE: <code><c:out value="${mailMode}"/></code> &middot;
               Subscribers: <b>${users.size()}</b></p>

            <form action="<c:url value='/admin'/>" method="post" class="form">
                <input type="hidden" name="action" value="broadcast">
                <input type="hidden" name="csrf" value="<c:out value='${sessionScope.csrfToken}'/>">
                <label for="subject">Subject</label>
                <input id="subject" type="text" name="subject" maxlength="200" required
                       value="<c:out value='${param.subject}'/>">
                <label for="body">Body</label>
                <textarea id="body" name="body" rows="8" required><c:out value="${param.body}"/></textarea>
                <span class="label">Format</span>
                <div class="radios">
                    <label><input type="radio" name="format" value="text" checked> text/plain</label>
                    <label><input type="radio" name="format" value="html"> text/html</label>
                </div>
                <button type="submit">Send to all subscribers (BCC)</button>
            </form>

            <h2>Subscribers</h2>
            <table class="info">
                <tr><th>#</th><th>First Name</th><th>Last Name</th><th>Email</th></tr>
                <c:forEach var="u" items="${users}" varStatus="s">
                    <tr><td>${s.count}</td><td><c:out value="${u.firstName}"/></td>
                        <td><c:out value="${u.lastName}"/></td><td><c:out value="${u.email}"/></td></tr>
                </c:forEach>
            </table>

            <form action="<c:url value='/admin'/>" method="post">
                <input type="hidden" name="action" value="logout">
                <input type="hidden" name="csrf" value="<c:out value='${sessionScope.csrfToken}'/>">
                <button type="submit" class="secondary">Log out</button>
            </form>
        </c:otherwise>
    </c:choose>
</section>

<%@ include file="/WEB-INF/includes/footer.jsp" %>
