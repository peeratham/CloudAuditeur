<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %> 
<html>
<head>
<title>Auditeur-Lite</title>
</head>

<body>

<p>
<h1>Auditeur-Lite</h1>
</p>

<p>
<a href="/">Homepage</a>||
<a href="/view-models">View Models</a>
</p>

<div>
<p>
positiveDataSet = (lookforTag1 AND lookforTag2 AND ...) AND (withinTag1 OR withinTag2 OR ...) <br>
negativeDataset = (withinTag1 OR withinTag2 OR ...) - positiveDataSet
</p>

<p>
${user.email} <br>
Separate each tag by comma (,)
</p>

<c:if test="${user != null}">
<form action="/request" method="post">

<input type="hidden" name="userEmail" value="${user.email}" />

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