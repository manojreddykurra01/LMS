$ticks = (Get-Date).Ticks
$user = "testuser" + $ticks
$phone = $ticks.ToString().Substring(0, 10)

$body = @{
    username = $user
    password = "password123"
    email = "$user@example.com"
    phone = $phone
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/auth/signup" -Method Post -Body $body -ContentType "application/json" | Out-Null
Write-Host "Registered"

$loginBody = @{
    username = $user
    password = "password123"
} | ConvertTo-Json

$loginRes = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/signin" -Method Post -Body $loginBody -ContentType "application/json"
$token = $loginRes.token
$userId = $loginRes.id
Write-Host "Token: $token"

$headers = @{
    Authorization = "Bearer $token"
}

$books = Invoke-RestMethod -Uri "http://localhost:8080/api/books" -Headers $headers -Method Get
if ($books.Length -eq 0) {
    Write-Host "No books found"
    exit
}

$bookId = $books[0].id
Write-Host "Borrowing book: $bookId"

try {
    $borrowRes = Invoke-RestMethod -Uri "http://localhost:8080/api/borrowings/borrow?userId=$userId&bookId=$bookId" -Method Post -Headers $headers -ContentType "application/json"
    Write-Host "Success!"
    $borrowRes | ConvertTo-Json
} catch {
    Write-Host "Error: $($_.Exception.Message)"
    $reader = new-object System.IO.StreamReader $_.Exception.Response.GetResponseStream()
    $reader.ReadToEnd()
}
