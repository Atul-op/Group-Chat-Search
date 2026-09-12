let allMessages = [];

async function loadChat() {
    try {
        const response = await fetch('/api/messages');
        allMessages = await response.json();
        const container = document.getElementById('chatContainer');
        container.innerHTML = '';

        if (allMessages.length === 0) {
            container.innerHTML = '<div class="loading">Database is empty. Click "Init Data" to generate messages.</div>';
            return;
        }

        allMessages.forEach(msg => {
            const date = new Date(msg.timestamp).toLocaleString();
            const msgDiv = document.createElement('div');
            msgDiv.className = 'message received';
            msgDiv.id = `msg-${msg.id}`;
            msgDiv.innerHTML = `
                <div class="sender">${msg.senderName}</div>
                <div class="content">${msg.content}</div>
                <div class="time">${date}</div>
            `;
            container.appendChild(msgDiv);
        });
        container.scrollTop = container.scrollHeight;
    } catch (error) {
        console.error("Failed to load chat", error);
    }
}

async function handleSearch(event) {
    if (event.key === 'Enter') {
        const query = document.getElementById('searchInput').value;
        const resultsContainer = document.getElementById('searchResults');
        if (!query.trim()) return;

        resultsContainer.innerHTML = '<div class="loading">Searching via vectors...</div>';

        try {
            // Updated to POST method to match DTO structure
            const response = await fetch('/api/search', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ query: query, limit: 5 })
            });
            const results = await response.json();

            resultsContainer.innerHTML = '';
            if (results.length === 0) {
                resultsContainer.innerHTML = '<div class="loading">No semantic matches found.</div>';
                return;
            }

            results.forEach((msg, index) => {
                const div = document.createElement('div');
                div.className = 'result-item';
                div.innerHTML = `<strong>${msg.senderName}:</strong> ${msg.content}<br><small>${new Date(msg.timestamp).toLocaleDateString()}</small>`;
                div.onclick = () => jumpToMessage(msg.id);
                resultsContainer.appendChild(div);

                if (index === 0) jumpToMessage(msg.id);
            });
        } catch (error) {
            resultsContainer.innerHTML = '<div class="loading" style="color:red;">Error performing search.</div>';
        }
    }
}

function jumpToMessage(id) {
    document.querySelectorAll('.highlighted').forEach(el => el.classList.remove('highlighted'));
    const target = document.getElementById(`msg-${id}`);
    if (target) {
        target.scrollIntoView({ behavior: 'smooth', block: 'center' });
        target.classList.add('highlighted');
        setTimeout(() => target.classList.remove('highlighted'), 4000);
    }
}

async function generateData() {
    const btn = document.getElementById('generateBtn');
    btn.disabled = true;
    btn.innerText = 'Generating...';
    try {
        const response = await fetch('/api/generate-data', { method: 'POST' });
        alert(await response.text());
        loadChat();
    } catch (e) {
        alert('Error generating data');
    }
    btn.disabled = false;
    btn.innerText = 'Init Data';
}

window.onload = loadChat;