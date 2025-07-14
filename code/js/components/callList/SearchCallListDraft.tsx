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
    const [loading, setLoading] = useState<boolean>(false);
    const [error, setError] = useState<string | null>(null);
    const [activeTab, setActiveTab] = useState<"callList" | "confirmation" | "sealedCallList" | "finalJury" | null>(null);

    useEffect(() => {
        if (currentRole && currentRole !== "Arbitration_Council") {
            navigate("/");
        }
    }, [currentRole, navigate]);

    const fetchCallLists = async (state: "callList" | "confirmation" | "sealedCallList" | "finalJury") => {
        setLoading(true);
        setActiveTab(state);
        try {
            const token = getCookie("token");
            if (!token) {
                setError("Token não encontrado. Faça login novamente.");
                setLoading(false);
                return;
            }

            const response = await fetch(`/arbnet/callListDraft/get?callType=${state}`, {
                method: "GET",
                headers: {
                    token,
                    "Content-Type": "application/json"
                }
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.title || "Erro ao obter convocatórias");
            }

            const data = await response.json();
            const mapped: CallListDraft[] = Array.isArray(data)
                ? data.map((item: any) => ({
                    callListId: item.callListId,
                    competitionName: item.competitionName,
                    deadline: item.deadline,
                    callType: item.callType,
                    userName: item.userName,
                    userEmail: item.userEmail,
                }))
                : [];

            setCallLists(mapped);
            setError(null);
        } catch (err) {
            setError(err instanceof Error ? err.message : "Erro inesperado.");
        } finally {
            setLoading(false);
        }
    };

    const title =
        activeTab === "callList"
            ? "Convocatórias em Rascunho/CallList"
            : activeTab === "confirmation"
                ? "Convocatórias Confirmadas"
                : activeTab === "sealedCallList"
                    ? "Convocatórias em Confirmação"
                    : activeTab === "finalJury"
                        ? "Convocatórias Finais"
                        : "Convocatórias";



    return (
        <div className="search-call-list-draft-container">
            <h2>{title}</h2>

            <div style={{ marginBottom: "1em" }}>
                <button
                    onClick={() => fetchCallLists("callList")}
                    className={activeTab === "callList" ? "active-tab" : ""}
                >
                    Em rascunho/callList
                </button>
                <button
                onClick={() => fetchCallLists("sealedCallList")}
                className={activeTab === "sealedCallList" ? "active-tab" : ""}
                >
                    Em confirmação
                </button>
                <button
                    onClick={() => fetchCallLists("confirmation")}
                    className={activeTab === "confirmation" ? "active-tab" : ""}
                >
                    Confirmadas
                </button>
                <button
                    onClick={() => fetchCallLists("finalJury")}
                    className={activeTab === "finalJury" ? "active-tab" : ""}
                >
                    Convocatórias Finais
                </button>


            </div>


            {loading ? (
                <div className="form-container">
                    <h3>Carregando convocatórias...</h3>
                </div>
            ) : error ? (
                <div className="form-container">
                    <h3>Erro</h3>
                    <p className="error-message">{error}</p>
                    <button onClick={() => navigate("/")}>Voltar ao Início</button>
                </div>
            ) : callLists.length === 0 && activeTab ? (
                <p>Nenhuma convocatória encontrada.</p>
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
