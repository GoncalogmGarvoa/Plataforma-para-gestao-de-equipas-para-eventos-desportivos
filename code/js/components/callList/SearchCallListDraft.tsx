import * as React from "react";
import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { useCurrentRole } from "../../src/context/Referee";
import "../SearchCallListDraft.css";

interface CallListDraft {
    callListId: number;
    competitionName: string;
    deadline: string;
    callType: string;
    userName: string;
    userEmail: string;
}

function getCookie(name: string): string | undefined {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) {
        return parts.pop()?.split(";").shift();
    }
    return undefined;
}

export function SearchCallListDraft() {
    const navigate = useNavigate();
    const currentRole = useCurrentRole();
    const [callLists, setCallLists] = useState<CallListDraft[]>([]);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);


    useEffect(() => {
        if (currentRole && currentRole !== "Arbitration_Council") {
            navigate("/");
            return;
        }
    }, [currentRole, navigate]);

    useEffect(() => {
        const fetchCallListDrafts = async () => {
            try {
                const token = getCookie("token");

                if (!token) {
                    setError("Token não encontrado. Faça login novamente.");
                    setLoading(false);
                    return;
                }

                const response = await fetch("/arbnet/callListDraft/get", {
                    method: "GET",
                    headers: {
                        token: token,
                        "Content-Type": "application/json"
                    }
                });

                console.log("API Response:", response);

                if (!response.ok) {
                    const errorData = await response.json();
                    console.error("Erro da API:", errorData);
                    throw new Error(errorData.title || "Erro ao obter convocatórias draft");
                }

                const data = await response.json();
                //console.log("Dados recebidos (JSON):", data);

                const rawList = Array.isArray(data) ? data : [];
                //console.log("Valor da lista de drafts:", rawList);

                const mappedList: CallListDraft[] = rawList.map((item: any) => ({
                    callListId: item.callListId,
                    competitionName: item.competitionName,
                    deadline: item.deadline,
                    callType: item.callType,
                    userName: item.userName,
                    userEmail: item.userEmail,
                }));

                setCallLists(mappedList);


                setLoading(false);
            } catch (err) {
                console.error("Erro no bloco catch:", err);
                setError(err instanceof Error ? err.message : "Erro inesperado.");
                setLoading(false);
            }
        };

        if (currentRole === "Arbitration_Council") {
            fetchCallListDrafts();
        }
    }, [currentRole]);

    console.log("Estado antes de renderizar:", { loading, error, callLists });

    if (currentRole !== "Arbitration_Council") {
        return null;
    }

    if (loading) {
        return (
            <div className="center-container">
                <div className="form-container">
                    <h2>Carregando Convocatórias Draft...</h2>
                </div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="center-container">
                <div className="form-container">
                    <h2>Erro</h2>
                    <p className="error-message">{error}</p>
                    <button onClick={() => navigate("/")}>Voltar ao Início</button>
                </div>
            </div>
        );
    }

    return (
        <div className="search-call-list-draft-container">
            <h2>Convocatórias em Draft</h2>

            {callLists.length === 0 ? (
                <div className="no-call-lists">
                    <p>Nenhuma convocatória draft encontrada.</p>
                </div>
            ) : (
                <div className="call-lists-grid">
                    {callLists.map((callList) => (
                        <div key={callList.callListId} className="call-list-card">
                            <div className="call-list-header">
                                <h3>{callList.competitionName}</h3>
                                <span className="call-list-id">#{callList.callListId}</span>
                            </div>

                            <div className="call-list-details">
                                <div className="detail-item">
                                    <strong>Nome da Competição:</strong> {callList.competitionName}
                                </div>
                                <div className="detail-item">
                                    <strong>Responsável:</strong> {callList.userName} ({callList.userEmail})
                                </div>
                                <div className="detail-item">
                                    <strong>Data Limite:</strong> {new Date(callList.deadline).toLocaleDateString()}
                                </div>
                            </div>

                            <div className="call-list-actions">
                                <button
                                    className="btn btn-secondary"
                                    onClick={() => navigate(`/edit-calllist/${callList.callListId}`)}
                                >
                                    Editar
                                </button>
                            </div>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
}
