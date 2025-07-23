import React, { useEffect, useState } from "react";
import { useParams, useLocation } from "react-router-dom";
import { getCookie } from "../../src/context/Authn";

export function CreatePaymentReport() {
    const { callListId } = useParams();
    const location = useLocation();
    const [loading, setLoading] = useState(true);
    const [eventData, setEventData] = useState<any>(null);
    const [form, setForm] = useState<any>({
        reportType: "Relatório de Pagamentos",
        sealed: false,
        juryRefere: "",
        paymentCoverSheet: {
            style: "",
            councilName: "",
            eventName: "",
            venue: "",
            eventDate: "",
            eventTime: "",
            location: "",
            organization: ""
        },
        paymentPerReferee: []
    });

    // Fetch callList
    useEffect(() => {
        const fetchCallList = async () => {
            setLoading(true);
            try {
                const token = getCookie("token");
                const res = await fetch(`/arbnet/callList/get/${callListId}`, {
                    headers: {
                        Authorization: `bearer ${getCookie("token")}`,
                    }
                });
                if (!res.ok) throw new Error("Erro ao buscar convocatória");
                const data = await res.json();
                setEventData(data);

                const firstDate = data.matchDays?.[0]?.matchDate || "";
                const parsedDate = firstDate ? new Date(firstDate).toISOString().split("T")[0] : "";

                const groupedParticipantsMap = new Map();

                (data.participants || []).forEach((p: any) => {
                    if (!groupedParticipantsMap.has(p.userId)) {
                        groupedParticipantsMap.set(p.userId, {
                            userId: p.userId,
                            name: p.userName,
                        });
                    }
                });

                const defaultReferees = Array.from(groupedParticipantsMap.values()).map((ref: any) => ({
                    name: ref.name,
                    nib: "",
                    sessionsPresence: (data.matchDaySessions || []).map((md: any) => ({
                        matchDay: md.id,
                        morning: false,
                        morningTime: "",
                        afternoon: false,
                        afternoonTime: "",
                    }))
                }));


                setForm((prev: any) => ({
                    ...prev,
                    competitionId: data.competitionId,
                    paymentCoverSheet: {
                        ...prev.paymentCoverSheet,
                        eventName: data.competitionName,
                        location: data.location,
                        eventDate: parsedDate,
                        councilName: data.association,
                        organization: data.association,
                    },
                    paymentPerReferee: defaultReferees
                }));
            } catch (e) {
                console.error(e);
            } finally {
                setLoading(false);
            }
        };

        if (callListId) fetchCallList();
    }, [callListId]);

    const handleChange = (field: string, value: any) => {
        setForm((prev: any) => ({ ...prev, [field]: value }));
    };

    const handleCoverChange = (field: string, value: string) => {
        setForm((prev: any) => ({
            ...prev,
            paymentCoverSheet: {
                ...prev.paymentCoverSheet,
                [field]: value
            }
        }));
    };

    const handleRefereeChange = (idx: number, field: string, value: string) => {
        setForm((prev: any) => {
            const refs = [...prev.paymentPerReferee];
            refs[idx] = { ...refs[idx], [field]: value };
            return { ...prev, paymentPerReferee: refs };
        });
    };

    const handlePresenceChange = (refIdx: number, sessionIdx: number, period: "morning" | "afternoon", value: boolean) => {
        setForm((prev: any) => {
            const refs = [...prev.paymentPerReferee];
            const sessions = [...refs[refIdx].sessionsPresence];
            sessions[sessionIdx] = {
                ...sessions[sessionIdx],
                [period]: value
            };
            refs[refIdx].sessionsPresence = sessions;
            return { ...prev, paymentPerReferee: refs };
        });
    };

    const handleTimeChange = (refIdx: number, sessionIdx: number, field: "morningTime" | "afternoonTime", value: string) => {
        setForm((prev: any) => {
            const refs = [...prev.paymentPerReferee];
            const sessions = [...refs[refIdx].sessionsPresence];
            sessions[sessionIdx][field] = value;
            refs[refIdx].sessionsPresence = sessions;
            return { ...prev, paymentPerReferee: refs };
        });
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        const token = getCookie("token");

        try {
            const response = await fetch("/arbnet/payments/create", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `bearer ${getCookie("token")}`,
                },
                body: JSON.stringify(form)
            });

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                console.error("Erro do servidor:", errorData);
                alert("Erro ao submeter relatório de pagamento.");
                return;
            }

            alert("Relatório de pagamento enviado com sucesso!");
        } catch (err) {
            console.error("Erro de rede:", err);
            alert("Erro ao submeter relatório de pagamento.");
        }
    };


    if (loading) return <div>Carregando...</div>;
    if (!eventData) return <div>Erro ao carregar convocatória.</div>;

    return (
        <div style={{ maxWidth: 900, margin: "2rem auto", padding: "2rem", background: "#fff", borderRadius: 8 }}>
            <h2>Criar Relatório de Pagamentos</h2>
            <form onSubmit={handleSubmit} style={{ display: "flex", flexDirection: "column", gap: "1rem" }}>
                <label>
                    Juiz Árbitro responsável:
                    <input
                        value={form.juryRefere}
                        onChange={e => handleChange("juryRefere", e.target.value)}
                        required
                    />
                </label>
                <fieldset style={{ padding: 12 }}>
                    <legend><strong>Dados do Evento</strong></legend>
                    <label>Nome da Prova: <input value={form.paymentCoverSheet.eventName} readOnly /></label>
                    <label>Data: <input type="date" value={form.paymentCoverSheet.eventDate} onChange={e => handleCoverChange("eventDate", e.target.value)} /></label>
                    <label>Hora: <input type="time" value={form.paymentCoverSheet.eventTime} onChange={e => handleCoverChange("eventTime", e.target.value)} /></label>
                    <label>Local: <input value={form.paymentCoverSheet.location} onChange={e => handleCoverChange("location", e.target.value)} /></label>
                    <label>Concelho: <input onChange={e => handleCoverChange("councilName", e.target.value)} /></label>
                    <label>Organização: <input value={form.paymentCoverSheet.organization} onChange={e => handleCoverChange("organization", e.target.value)} /></label>
                    <label>Estabelecimento: <input onChange={e => handleCoverChange("venue", e.target.value)} /></label>
                </fieldset>

                <div>
                    <h3>Presenças e NIBs dos Árbitros</h3>
                    {form.paymentPerReferee.map((ref: any, idx: number) => (
                        <div
                            key={idx}
                            style={{
                                border: "1px solid #ccc",
                                padding: 12,
                                marginBottom: 12,
                                borderRadius: 4,
                            }}
                        >
                            <strong>{ref.name}</strong>
                            <div style={{ display: "flex", flexDirection: "column", gap: 6, marginTop: 6 }}>
                                <div style={{ display: "flex", alignItems: "center", gap: 12 }}>
                                    <label>
                                        NIB:{" "}
                                        <input
                                            value={ref.nib}
                                            onChange={(e) => handleRefereeChange(idx, "nib", e.target.value)}
                                            style={{ width: 200 }}
                                        />
                                    </label>

                                    <label>
                                        Nº de refeições:{" "}
                                        <input
                                            type="number"
                                            value={ref.meals ?? ""}
                                            onChange={(e) => handleRefereeChange(idx, "meals", e.target.value)}
                                            style={{ width: 100 }}
                                            min={0}
                                        />
                                    </label>

                                    <label>
                                        Valor pago:{" "}
                                        <input
                                            type="number"
                                            step="0.01"
                                            value={ref.amountPaid ?? ""}
                                            onChange={(e) => handleRefereeChange(idx, "amountPaid", e.target.value)}
                                            style={{ width: 120 }}
                                            min={0}
                                        />
                                    </label>
                                </div>
                            </div>

                            <div style={{ marginTop: 10 }}>
                                {ref.sessionsPresence.map((sess: any, sidx: number) => (
                                    <div
                                        key={sidx}
                                        style={{ display: "flex", gap: 12, alignItems: "center" }}
                                    >
                                        <span style={{ fontWeight: 500 }}>Jornada {sess.matchDay}</span>
                                        <label>
                                            <input
                                                type="checkbox"
                                                checked={sess.morning}
                                                onChange={(e) =>
                                                    handlePresenceChange(idx, sidx, "morning", e.target.checked)
                                                }
                                            />
                                            Manhã
                                        </label>
                                        {sess.morning && (
                                            <input
                                                type="time"
                                                value={sess.morningTime}
                                                onChange={(e) =>
                                                    handleTimeChange(idx, sidx, "morningTime", e.target.value)
                                                }
                                            />
                                        )}
                                        <label>
                                            <input
                                                type="checkbox"
                                                checked={sess.afternoon}
                                                onChange={(e) =>
                                                    handlePresenceChange(idx, sidx, "afternoon", e.target.checked)
                                                }
                                            />
                                            Tarde
                                        </label>
                                        {sess.afternoon && (
                                            <input
                                                type="time"
                                                value={sess.afternoonTime}
                                                onChange={(e) =>
                                                    handleTimeChange(idx, sidx, "afternoonTime", e.target.value)
                                                }
                                            />
                                        )}
                                    </div>
                                ))}
                            </div>
                        </div>
                    ))}
                </div>


                <button type="submit" style={{ background: "#1976d2", color: "white", padding: "0.6rem 1.4rem", fontWeight: 600, border: "none", borderRadius: 4 }}>
                    Submeter Relatório
                </button>
            </form>
        </div>
    );
}
