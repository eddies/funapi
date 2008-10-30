<%
String baseURL = String.format("%s://%s:%s%s", 
                               request.getScheme(), 
                               request.getServerName(),
                               request.getServerPort(),
                               request.getContextPath());

String pathInfo = request.getPathInfo();

String service = "";
String id = "";
String server = "";

if (pathInfo != null && pathInfo.length() > 1) {
    // expecting pathInfo to look like: "/fedora/info:fedora/demo:1"
    String[] tmp = pathInfo.substring(1).split("/", 2);
    if (tmp.length == 2) {
        service = tmp[0];
        id = tmp[1];
        server = String.format("%s/%s", baseURL, service);
    }
}
%>


<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN"
"http://www.w3.org/TR/html4/strict.dtd">
<html>
<head>
    <title>funAPI Echo Service</title>
    <link rel="unapi-server" type="application/xml" title="unAPI" href="<%=server%>" />
</head>
<body>
    <div>
        <abbr class="unapi-id" title="<%=id%>"></abbr> 
    </div>
    <h1>funAPI Echo Service</h1>
    <p>This service is used to generate the information required by unAPI 
    clients to discover information about an unAPI resource. 
    Typically, you would be responsible for generating the autodiscovery and
    microformat identifiers, but this service may be useful for testing.</p>
    
    <p>To use this service, issue a request as follows:
    <blockquote>
    <%=baseURL%><%=request.getServletPath()%>/<i>service</i>/<i>id</i>
    </blockquote>
    substituting the funAPI service (e.g. fedora or pmh-dspace) for 
    <i>service</i> and the id of the resource (e.g. info:fedora/demo:foo) for 
    <i>id</i>.</p>
    
    <p>The above request will return this page, with &lt;link&gt; and 
    &lt;abbr&gt; elements populated with the provided service and id parameters. 
    Browsers don't normally render such elements, so you would need to view the 
    source of this web page to see the actual results.</p>
</body>
</html>
