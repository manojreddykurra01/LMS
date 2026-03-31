$loginBody = @{ username = 'admin01'; password = 'Admin@123' } | ConvertTo-Json
$loginRes = Invoke-RestMethod -Uri 'http://localhost:8080/api/auth/signin' -Method POST -Body $loginBody -ContentType 'application/json'
Write-Host 'Token:' $loginRes.token
$headers = @{ Authorization = "Bearer $($loginRes.token)" }
Invoke-RestMethod -Uri 'http://localhost:8080/api/users' -Method GET -Headers $headers | ConvertTo-Json -Depth 2
