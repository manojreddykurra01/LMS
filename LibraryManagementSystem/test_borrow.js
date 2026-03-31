const username = "testuser" + Date.now();
const password = "password123";

async function run() {
    try {
        console.log("Registering user...");
        await fetch('http://localhost:8080/api/auth/signup', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                username: username,
                password: password,
                email: username + "@example.com",
                phone: "1234567890"
            })
        });

        console.log("Signing in...");
        const loginRes = await fetch('http://localhost:8080/api/auth/signin', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });
        const loginData = await loginRes.json();
        console.log("Login Token:", loginData.token);
        
        console.log("Fetching books...");
        const booksRes = await fetch('http://localhost:8080/api/books', {
            headers: { 'Authorization': 'Bearer ' + loginData.token }
        });
        const books = await booksRes.json();
        
        if (books.length === 0) {
            console.log("No books found. Please add a book to the DB first.");
            return;
        }
        
        console.log("Borrowing book id:", books[0].id);
        const borrowRes = await fetch(`http://localhost:8080/api/borrowings/borrow?userId=${loginData.id}&bookId=${books[0].id}`, {
            method: 'POST',
            headers: { 'Authorization': 'Bearer ' + loginData.token }
        });
        
        if (!borrowRes.ok) {
            console.error("Failed to borrow book. Status:", borrowRes.status);
            const err = await borrowRes.text();
            console.error(err);
        } else {
            console.log(await borrowRes.json());
        }
    } catch(e) {
        console.error("Test failed", e);
    }
}
run();
