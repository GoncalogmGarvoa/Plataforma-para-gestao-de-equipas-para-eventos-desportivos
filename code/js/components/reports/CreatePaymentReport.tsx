import React, { useEffect, useState } from "react";
import { useParams, useLocation } from "react-router-dom";
import { getCookie } from "../../src/context/Authn";

interface PaymentReportInputModel {
    id?: string | null;
    competitionId: number;
    sealed?: boolean;
    juryRefere: string;
    paymentCoverSheet: PaymentCoverSheet;
    paymentInfoPerReferee: PaymentInfoPerReferee[];
}

interface PaymentCoverSheet {
    style: string;
    councilName: string;
    eventName: string;
    venue: string;
    eventDate: string; // "YYYY-MM-DD"
    eventTime: string; // "HH:mm"
    location: string;
    organization: string;
}

interface PaymentInfoPerReferee {
    name: string;
    nib: string;
    numberOfMeals: number;
    payedAmount: number;
    sessionsPresence: SessionsPresence[];
}

interface SessionsPresence {
    matchDay: number;
    morning: boolean;
    morningTime: string; // "HH:mm"
    afternoon: boolean;
    afternoonTime: string; // "HH:mm"
}

export function CreatePaymentReport() {
    const { callListId } = useParams();
    const location = useLocation();
    const [loading, setLoading] = useState(true);
    const [eventData, setEventData] = useState<any>(null);
    const [form, setForm] = useState<PaymentReportInputModel>({
        competitionId: 0,
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
        paymentInfoPerReferee: []
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

                const firstDate = data.matchDaySessions?.[0].matchDate;
                const firstTime = data.matchDaySessions?.[0].sessions?.[0]?.startTime;

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
                    numberOfMeals: 0,
                    payedAmount: 0,
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
                    competitionId: data.matchDaySessions[0].competitionId,
                    paymentCoverSheet: {
                        ...prev.paymentCoverSheet,
                        eventName: data.competitionName,
                        location: data.address,
                        venue: data.location,
                        eventDate: firstDate,
                        eventTime: firstTime,
                        councilName: data.association,
                    },
                    paymentInfoPerReferee: defaultReferees
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

    const handleRefereeChange = (idx: number, field: string, value: any) => {
        setForm((prev) => {
            const refs = [...prev.paymentInfoPerReferee];
            refs[idx] = {
                ...refs[idx],
                [field]: ["numberOfMeals", "payedAmount"].includes(field)
                    ? Number(value)
                    : value
            };
            return { ...prev, paymentInfoPerReferee: refs };
        });
    };

    const handlePresenceChange = (refIdx: number, sessionIdx: number, period: "morning" | "afternoon", value: boolean) => {
        setForm((prev: any) => {
            const refs = [...prev.paymentInfoPerReferee];
            const sessions = [...refs[refIdx].sessionsPresence];
            sessions[sessionIdx] = {
                ...sessions[sessionIdx],
                [period]: value
            };
            refs[refIdx].sessionsPresence = sessions;
            return { ...prev, paymentInfoPerReferee: refs };
        });
    };

    const handleTimeChange = (refIdx: number, sessionIdx: number, field: "morningTime" | "afternoonTime", value: string) => {
        setForm((prev: any) => {
            const refs = [...prev.paymentInfoPerReferee];
            const sessions = [...refs[refIdx].sessionsPresence];
            sessions[sessionIdx][field] = value;
            refs[refIdx].sessionsPresence = sessions;
            return { ...prev, paymentInfoPerReferee: refs };
        });
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        const token = getCookie("token");

        // Garantir que o form tem o tipo correto
        const payload: PaymentReportInputModel = {
            ...form,
            sealed: form.sealed ?? false, // garantir que existe mesmo que seja opcional
            paymentInfoPerReferee: form.paymentInfoPerReferee.map((ref) => ({
                name: ref.name,
                nib: ref.nib,
                numberOfMeals: ref.numberOfMeals,
                payedAmount: ref.payedAmount,
                sessionsPresence: ref.sessionsPresence.map((sess) => ({
                    matchDay: sess.matchDay,
                    morning: sess.morning,
                    morningTime: sess.morningTime,
                    afternoon: sess.afternoon,
                    afternoonTime: sess.afternoonTime
                })) as SessionsPresence[]
            })) as PaymentInfoPerReferee[]
        };

        try {
            const response = await fetch("/arbnet/payments/create", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `bearer ${getCookie("token")}`,
                },
                body: JSON.stringify(payload)
            });

            if (!response.ok) {
                throw new Error("Erro ao submeter relatório.");
            }

            alert("Relatório de pagamento enviado!");
        } catch (err) {
            alert("Erro ao submeter relatório.");
            console.error(err);
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
                    <label>Morada: <input value={form.paymentCoverSheet.location} onChange={e => handleCoverChange("location", e.target.value)} /></label>
                    <label>Conselho: <input value = {form.paymentCoverSheet.councilName} onChange={e => handleCoverChange("councilName", e.target.value)} /></label>
                    <label>Associação: <input  onChange={e => handleCoverChange("organization", e.target.value)} /></label>
                    <label>Estabelecimento: <input value = {form.paymentCoverSheet.venue} onChange={e => handleCoverChange("venue", e.target.value)} /></label>
                </fieldset>

                <div>
                    <h3>Presenças e NIBs dos Árbitros</h3>
                    {form.paymentInfoPerReferee.map((ref: any, idx: number) => (
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
                                            value={ref.numberOfMeals ?? ""}
                                            onChange={(e) => handleRefereeChange(idx, "numberOfMeals", e.target.value)}
                                            style={{ width: 100 }}
                                            min={0}
                                        />
                                    </label>

                                    <label>
                                        Valor pago:{" "}
                                        <input
                                            type="number"
                                            step="0.01"
                                            value={ref.payedAmount ?? ""}
                                            onChange={(e) => handleRefereeChange(idx, "payedAmount", e.target.value)}
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
