<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %> 
<html>
<head>
<title>Auditeur-Models</title>
</head>

<body>

<p>
<h2>Auditeur-Models</h2>
</p>

<p>
<a href="/">Homepage</a>||
<a href="/request">Request for model training</a>
		
</p>

<c:choose>
			<c:when test="${hasModels}">
				<h2>Your Models</h2>
				<form action="/delete" method="post">
					<input type="hidden" name="kind" value="model" />
					<ul>
						<c:forEach var="model" items="${models}"> 
							<li>
								<input type="checkbox" name="delete" value="${model.modelKey}" />
								|lookfor : ${model.lookfor} | within : ${model.within} | accuracy : ${model.accuracy} |
								<a href="/download?key=${model.modelKey}&kind=model">Model file</a>|
								<a href="/download?key=${model.modelKey}&kind=range">Range file</a>|
							</li>
						</c:forEach>
					</ul>
					<input type="submit" value="Delete Selected" />
				</form> 
			</c:when>
			<c:otherwise>
			<p>You have no models.</p>
			</c:otherwise>
</c:choose>
</body>
</html>

