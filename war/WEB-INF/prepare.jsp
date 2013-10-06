<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %> 
<html>
<head>
<title>Auditeur-Lite</title>
</head>

<body>

<p>
<h2>Auditeur-Lite</h2>
</p>

<div>


<c:if test="${user != null}">
<form action="/query" method="post">

<input type="hidden" value="${user.email}" name="user_email" />

<label for="lookfor">Lookfor Tags:</label>
<input name="lookfor" id="lookfor" type="text" size="20"/> 

<label for="within">Within Tags:</label>
<input name="within" id="within" type="text" size="20"/>
<input type="submit" value="Set" />

</form> 
</c:if>

	
	


</div>

</body>
</html>