import { useNavigate } from "react-router-dom";
import { useSetUser } from "../../src/context/Authn";
import { useSetEmail } from "../../src/context/Referee";
import { useSetRole } from "../../src/context/Referee"
import { useEffect, useState } from "react";
import * as React from "react";



interface Role {
    id: number;
    name: string;
}

function getCookie(name: string): string | undefined {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) {
        return parts.pop()?.split(";").shift();
    }
    return undefined;
}

// Função utilitária para traduzir o nome do perfil
function getRoleLabel(roleName: string): string {
    switch (roleName) {
        case "Admin":
            return "Administrador";
        case "Arbitration_Council":
            return "Conselho de Arbitragem";
        case "Referee":
            return "Árbitro";
        default:
            return roleName;
    }
}

export function SelectRole() {
    const [roles, setRoles] = useState<Role[]>([]);
    const [selectedRole, setSelectedRole] = useState<number | null>(null);
    const [error, setError] = useState<string | null>(null);
    const [loading, setLoading] = useState<boolean>(true);
    const [hasExistingRole, setHasExistingRole] = useState<boolean>(false);

    const navigate = useNavigate();
    const setUser = useSetUser();
    const setEmail = useSetEmail();
    const setRole = useSetRole();

    useEffect(() => {
        if (getCookie("role")) {
            setHasExistingRole(true);
        }

        const fetchRoles = async () => {
            try {
                const token = getCookie("token");
                const email = getCookie("email");

                if (!token || !email) {
                    setError("Token ou email não encontrado. Faça login novamente.");
                    setLoading(false);
                    return;
                }

                const response = await fetch("/arbnet/users/roles/fromUser", {
                    headers: {
                        token: token,
                        "Content-Type": "application/json"
                    }
                });

                if (!response.ok) {
                    const errorData = await response.json();
                    throw new Error(errorData.title || "Erro ao obter roles");
                }

                const data = await response.json();

                if (!data.value || !Array.isArray(data.value)) {
                    throw new Error("Formato inesperado dos dados das roles");
                }

                setRoles(data.value);
                setLoading(false);
            } catch (err) {
                console.error("Erro ao buscar roles:", err);
                setError(err instanceof Error ? err.message : "Erro inesperado.");
                setLoading(false);
            }
        };

        fetchRoles();
    }, []);

    const handleRoleSelect = async (roleId: number) => {
        try {
            const token = getCookie("token");

            if (!token) {
                setError("Token de autenticação não encontrado.");
                return;
            }

            const response = await fetch("/arbnet/users/role/set", {
                method: "POST",
                headers: {
                    token: token,
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({ id: roleId })  // enviando JSON
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.title || "Erro ao definir role");
            }

            const selectedRoleData = roles.find((r: Role) => r.id === roleId);
            const roleName = selectedRoleData?.name || "";

            const expirationDate = new Date();
            expirationDate.setHours(expirationDate.getHours() + 1);
            document.cookie = `role=${roleName}; expires=${expirationDate.toUTCString()}; path=/;`;

            setUser("authenticated");
            setEmail(getCookie("email") || "");
            setRole(roleName);

            navigate("/");
        } catch (err) {
            console.error("Erro ao selecionar role:", err);
            setError(err instanceof Error ? err.message : "Erro inesperado ao definir role.");
        }
    };


    if (error) {
        return (
            <div className="error-container">
                <h2>Erro</h2>
                <p className="error-message">{error}</p>
                <button onClick={() => navigate(hasExistingRole ? "/" : "/login")}>Voltar {hasExistingRole ? "" : "ao Login"}</button>
            </div>
        );
    }
    return (
        <div className="role-selection">
            <h2>{hasExistingRole ? "Mudar o seu perfil" : "Selecione o seu perfil"}</h2>

            {loading ? (
                <p>A carregar roles...</p>
            ) : roles.length > 0 ? (
                <div className="roles-container">
                    <div className="roles-list">
                        {roles.map((role) => (
                            <div
                                key={role.id}
                                className={`role-card ${selectedRole === role.id ? "selected" : ""}`}
                                onClick={() => setSelectedRole(role.id)}
                            >
                                <h3>{getRoleLabel(role.name)}</h3>
                            </div>
                        ))}
                    </div>

                    {selectedRole && (
                        <button
                            className="select-role-button"
                            onClick={() => handleRoleSelect(selectedRole)}
                        >
                            {hasExistingRole ? "Mudar Perfil" : "Continuar"}
                        </button>
                    )}
                </div>
            ) : (
                <p>Nenhum perfil disponível.</p>
            )}
        </div>
    );
}