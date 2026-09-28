<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/includes/header.jsp" %>

<h1>Admin - Send to the whole list</h1>

<c:forEach var="e" items="${errors}">
    <p class="error"><i><c:out value="${e}"/></i></p>
</c:forEach>
<c:if test="${not empty message}">
    <p class="success"><c:out value="${message}"/></p>
</c:if>

<c:choose>
    <c:when test="${not adminEnabled}">
        <p><i>The admin page is disabled. Set the ADMIN_PASSWORD
           environment variable to enable it.</i></p>
    </c:when>
    <c:when test="${not loggedIn}">
        <form action="<c:url value='/admin'/>" method="post">
            <input type="hidden" name="action" value="login">
            <label class="pad_top">Password:</label>
            <input type="password" name="password" required><br>
            <label>&nbsp;</label>
            <input type="submit" value="Log in" class="margin_left">
        </form>
    </c:when>
    <c:otherwise>
        <p>MAIL_MODE: <c:out value="${mailMode}"/> - Subscribers: ${users.size()}</p>

        <form action="<c:url value='/admin'/>" method="post">
            <input type="hidden" name="action" value="broadcast">
            <input type="hidden" name="csrf" value="<c:out value='${sessionScope.csrfToken}'/>">
            <label class="pad_top">Subject:</label>
            <input type="text" name="subject" maxlength="200" required
                   value="<c:out value='${param.subject}'/>"><br>
            <label class="pad_top">Body:</label>
            <textarea name="body" rows="8" required><c:out value="${param.body}"/></textarea><br>
            <label class="pad_top">Format:</label>
            <span>
                <input type="radio" name="format" value="text" checked> text/plain
                <input type="radio" name="format" value="html"> text/html
            </span><br>
            <label>&nbsp;</label>
            <input type="submit" value="Send to all subscribers (BCC)" class="margin_left">
        </form>

        <h2>Subscribers</h2>
        <table>
            <tr><th>#</th><th>First Name</th><th>Last Name</th><th>Email</th></tr>
            <c:forEach var="u" items="${users}" varStatus="s">
                <tr><td>${s.count}</td><td><c:out value="${u.firstName}"/></td>
                    <td><c:out value="${u.lastName}"/></td><td><c:out value="${u.email}"/></td></tr>
            </c:forEach>
        </table>

        <form action="<c:url value='/admin'/>" method="post">
            <input type="hidden" name="action" value="logout">
            <input type="hidden" name="csrf" value="<c:out value='${sessionScope.csrfToken}'/>">
            <input type="submit" value="Log out">
        </form>
    </c:otherwise>
</c:choose>

<%@ include file="/WEB-INF/includes/footer.jsp" %>
