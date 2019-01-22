<%-- 
    Document   : index.jsp
    Created on : 24-ott-2018, 19.26.13
    Author     : Lorenzo
--%>
<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix = "c" uri = "http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <body>
        <h1>Pagina di Registrazione di Items'</h1>
        <form method="POST" action="<c:url value="/ItemRegistration">
                  <c:param name="action" value="addItem"/>
              </c:url>" enctype="multipart/form-data">
            Name <input type="text" name="Name"/>
            <br/>
            Logo <input type="file" name="File"/>
            <br/>
            Note <input type="text" name="Note"/>
            <br/>
            <select name="category">
                <c:forEach items="${categories}" var="cat">
                    <option value="${cat.getIdCategory()}">${cat.getName()}</option>
                </c:forEach>
            </select>
            <br/>
            <input type="submit" value="Submit">
        </form>
    </body>
</html>
