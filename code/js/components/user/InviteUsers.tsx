import * as React from "react";
import { useState } from "react";

export function InviteUsers() {
    const [email, setEmail] = useState("");
    const [loading, setLoading] = useState(false);
    const [message, setMessage] = useState<string | null>(null);
    const [error, setError] = useState<string | null>(null);

    const handleInvite = async () => {
        setLoading(true);
        setMessage(null);
        setError(null);

        // Validação simples de email
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(email)) {
            setError("Por favor insira um email válido.");
            setLoading(false);
            return;
        }

        try {
            const token = document.cookie
                .split("; ")
                .find((row) => row.startsWith("token="))
                ?.split("=")[1];

            if (!token) {
                setError("Token não encontrado. Faça login novamente.");
                setLoading(false);
                return;
            }

            const response = await fetch("/arbnet/users/invite", {
                method: "POST",
                headers: {
                    Authorization: `bearer ${token}`,
                    "Content-Type": "application/json",
                },
                body: JSON.stringify(email), // envia como string bruta
            });

            if (!response.ok) {
                const data = await response.json();
                throw new Error(data.title || "Erro ao convidar utilizador.");
            }

            setMessage("Convite enviado com sucesso!");
            setEmail("");
        } catch (err) {
            setError(err instanceof Error ? err.message : "Erro inesperado.");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="center-container">
            <div className="form-container">
                <p className="form-title">Convidar Utilizador</p>

                <input
                    type="email"
                    placeholder="Email do utilizador"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    disabled={loading}
                />

                <button
                    onClick={handleInvite}
                    disabled={loading || email.trim() === ""}
                    className="btn btn-primary"
                >
                    {loading ? "A Enviar..." : "Convidar"}
                </button>

                {message && <p className="success-message">{message}</p>}
                {error && <p className="error-message">{error}</p>}
            </div>
        </div>
    );
}
