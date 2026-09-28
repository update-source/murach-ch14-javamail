<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/includes/header.jsp" %>

<h1>Join our email list</h1>
<p>To join our email list, enter your name and
   email address below.</p>

<c:forEach var="e" items="${errors}">
    <p class="error"><i><c:out value="${e}"/></i></p>
</c:forEach>

<form action="<c:url value='/emailList'/>" method="post">
    <input type="hidden" name="action" value="add">
    <label class="pad_top">Email:</label>
    <input type="email" name="email" maxlength="254" required
           value="<c:out value='${user.email}'/>"><br>
    <label class="pad_top">First Name:</label>
    <input type="text" name="firstName" maxlength="50" required
           value="<c:out value='${user.firstName}'/>"><br>
    <label class="pad_top">Last Name:</label>
    <input type="text" name="lastName" maxlength="50" required
           value="<c:out value='${user.lastName}'/>"><br>
    <label class="pad_top">Email format:</label>
    <span>
        <input type="radio" name="format" value="text" ${format ne 'html' ? 'checked' : ''}> Plain text
        <input type="radio" name="format" value="html" ${format eq 'html' ? 'checked' : ''}> HTML
    </span><br>
    <label>&nbsp;</label>
    <input type="submit" value="Join Now" class="margin_left">
</form>

<%@ include file="/WEB-INF/includes/footer.jsp" %>
