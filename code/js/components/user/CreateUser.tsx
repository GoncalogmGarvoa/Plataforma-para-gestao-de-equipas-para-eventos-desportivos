import { useState, useEffect } from "react"
import * as React from "react"
import {Navigate, useLocation} from "react-router-dom"
import './CreateUser.css';

export function CreateUser(): JSX.Element {
    const [inputs, setInputs] = useState({
        name: "",
        phoneNumber: "",
        address: "",
        email: "",
        password: "",
        birthDate: "",
        iban: "",
    })
    const [isSubmitting, setIsSubmitting] = useState(false)
    const [error, setError] = useState<string | undefined>(undefined)
    const [redirect, setRedirect] = useState(false)
    const location = useLocation()
    const queryParams = new URLSearchParams(location.search)
    const inviteToken = queryParams.get("inviteToken")

    useEffect(() => {
        if (!inviteToken) {
            setError("Invalid invitation link: missing invite token.")
        }
    }, [inviteToken])

    if (redirect) {
        return <Navigate to={location.state?.source?.pathname || "/"} replace={true} />
    }

    function handleChange(ev: React.FormEvent<HTMLInputElement>) {
        const name = ev.currentTarget.name
        setInputs({ ...inputs, [name]: ev.currentTarget.value })
        setError(undefined)
    }

    function handleSubmit(ev: React.FormEvent<HTMLFormElement>) {
        ev.preventDefault()
        setIsSubmitting(true)

        const handleCreateUser = async () => {
            try {
                const response = await fetch("/arbnet/users/signup", {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json",
                    },
                    body: JSON.stringify({ ...inputs, inviteToken }),
                });

                const data = await response.json();
                setIsSubmitting(false);

                if (response.ok) {
                    setRedirect(true);
                } else {
                    setError(data.title || "Error creating User.");
                }
            } catch (error: any) {
                setIsSubmitting(false);
                setError(error.message);
                console.error("Error creating User:", error);
            }
        };
        handleCreateUser()
    }

    return (
        <div className="create-user-container">
            <div>
                <h1 className="create-user-title">Criar Conta</h1>
                {error && <p className="error-message">{error}</p>}
                <form onSubmit={handleSubmit} className="create-user-form">
                    <label>Nome Completo
                        <input type="text" name="name" placeholder="Insira o seu nome completo" value={inputs.name} onChange={handleChange} required />
                    </label>
                    <label>Número de Telemóvel
                        <input type="text" name="phoneNumber" placeholder="Insira o seu número de telemóvel" value={inputs.phoneNumber} onChange={handleChange} required />
                    </label>
                    <label>Morada
                        <input type="text" name="address" placeholder="Insira a sua morada" value={inputs.address} onChange={handleChange} required />
                    </label>
                    <label>Email
                        <input type="email" name="email" placeholder="Insira o seu email" value={inputs.email} onChange={handleChange} required />
                    </label>
                    <label>Palavra-passe
                        <input type="password" name="password" placeholder="Crie uma palavra-passe" value={inputs.password} onChange={handleChange} required />
                    </label>
                    <label>Data de Nascimento
                        <input type="date" name="birthDate" placeholder="dd/mm/aaaa" value={inputs.birthDate} onChange={handleChange} required />
                    </label>
                    <label>IBAN
                        <input type="text" name="iban" placeholder="Insira o seu IBAN" value={inputs.iban} onChange={handleChange} required />
                    </label>
                    <button type="submit" className="form-button" disabled={isSubmitting}>
                        Criar Conta
                    </button>
                </form>
            </div>
        </div>
    )
}