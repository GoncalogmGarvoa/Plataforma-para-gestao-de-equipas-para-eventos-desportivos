import React, { useState, useEffect } from "react";
import { useLocation, useParams } from "react-router-dom";
import { useCurrentUser } from "../../src/context/Authn";
import { getCookie } from "../../src/context/Authn";

export function CreateJuizReport() {
  const { callListId } = useParams();
  const location = useLocation();
  const currentUser = useCurrentUser() as { name?: string } | null;
  let tipoDocumento = "relatorio juiz arbitro";

  const now = new Date();
  const anoAtual = now.getFullYear();
  const epocaPadrao = `${anoAtual}/${anoAtual + 1}`;
  const hoje = new Date();
  const dataHoje = hoje.toISOString().split('T')[0];

  const [loading, setLoading] = useState(true);
  const [eventData, setEventData] = useState<any>(null);
  const [form, setForm] = useState<any>({
    tipoDocumento: tipoDocumento,
    estiloProva: "",
    epoca: epocaPadrao,
    nomeAutor: "",
    nomeCompeticao: "",
    localizacao: "",
    data: dataHoje,
    numeroJornadas: 0,
    numeroSessoes: 0,
    resumo: "",
    observacoes: "",
    avaliacoes: [],
    sessoes: []
  });
  const [jury, setJury] = useState<any[]>([]);
  const [positionOptions, setPositionOptions] = useState<{id: number, name: string}[]>([]);

  useEffect(() => {
    const fetchEvent = async () => {
      setLoading(true);
      try {
        const token = getCookie("token");
        const res = await fetch(`/arbnet/callList/get/${callListId}`, {
          headers: {
            Authorization: `bearer ${getCookie("token")}`,
          }
        });
        if (!res.ok) throw new Error("Erro ao buscar dados da convocatória");
        const data = await res.json();
        setEventData(data);
        const avaliacoes = (data.participants || []).map((p: any) => ({
          name: p.userName || p.name,
          category: p.category,
          grade: '',
          notes: '',
          functionBySession: {}
        }));
        const sessoes = (data.matchDaySessions || []).flatMap((md: any) =>
          (md.sessions || []).map((s: any) => ({
            date: md.matchDate,
            startTime: s.startTime || s.time || '',
            endTime: '',
            durationMinutes: ''
          }))
        );
        setForm((prev: any) => ({
          ...prev,
          estiloProva: '',
          epoca: epocaPadrao,
          nomeAutor: prev.nomeAutor || currentUser?.name || '',
          nomeCompeticao: data.competitionName || '',
          localizacao: data.location || '',
          data: dataHoje,
          numeroJornadas: data.matchDaySessions?.length || 0,
          numeroSessoes: sessoes.length,
          avaliacoes,
          sessoes
        }));
      } catch (e) {
      } finally {
        setLoading(false);
      }
    };
    if (callListId) fetchEvent();
  }, [callListId, currentUser]);

  useEffect(() => {
    const fetchAuthor = async () => {
      try {
        const token = getCookie("token");
        const res = await fetch('/arbnet/users/me', { headers: {
          Authorization: `bearer ${getCookie("token")}`,
        } });
        if (res.ok) {
          const data = await res.json();
          setForm((prev: any) => ({
            ...prev,
            nomeAutor: data.name || prev.nomeAutor
          }));
        }
      } catch {}
    };
    fetchAuthor();
  }, []);

  const fetchPositions = async () => {
    try {
      const token = getCookie("token");
      const res = await fetch("/arbnet/users/positions", {
        headers: {
          Authorization: `bearer ${getCookie("token")}`,
        }
      });
      console.log("Status positions fetch:", res.status);
      if (!res.ok) throw new Error("Erro ao buscar posições");
      const data = await res.json();
      console.log("Positions fetched:", data);
      setPositionOptions(data);
    } catch (err) {
      console.error("Erro ao buscar posições:", err);
      setPositionOptions([]);
    }
  };
  useEffect(() => {
    fetchPositions();
  }, []);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleAvaliacaoChange = (idx: number, field: string, value: string) => {
    setForm((prev: any) => {
      const avaliacoes = [...prev.avaliacoes];
      avaliacoes[idx] = { ...avaliacoes[idx], [field]: value };
      return { ...prev, avaliacoes };
    });
  };

  const handleSessaoChange = (idx: number, field: string, value: string) => {
    setForm((prev: any) => {
      const sessoes = [...prev.sessoes];
      sessoes[idx] = { ...sessoes[idx], [field]: value };
      return { ...prev, sessoes };
    });
  };

  const handleFunctionBySessionChange = (evalIdx: number, sessionId: string, value: string) => {
    setForm((prev: any) => {
      const avaliacoes = [...prev.avaliacoes];
      const fbSession = { ...(avaliacoes[evalIdx].functionBySession || {}) };
      fbSession[sessionId] = value;
      avaliacoes[evalIdx] = { ...avaliacoes[evalIdx], functionBySession: fbSession };
      return { ...prev, avaliacoes };
    });
  };

  const handleJuryChange = (juryIdx: number, field: string, value: string) => {
    setJury((prev) => {
      const arr = [...prev];
      arr[juryIdx] = { ...arr[juryIdx], [field]: value };
      return arr;
    });
  };
  const handleJuryMemberChange = (juryIdx: number, memberIdx: number, field: string, value: string) => {
    setJury((prev) => {
      const arr = [...prev];
      const members = [...(arr[juryIdx].juryMembers || [])];
      members[memberIdx] = { ...members[memberIdx], [field]: value };
      arr[juryIdx] = { ...arr[juryIdx], juryMembers: members };
      return arr;
    });
  };
  const addJurySession = () => {
    setJury((prev) => ([...prev, { matchDayId: '', sessionId: '', juryMembers: [] }]));
  };
  const addJuryMember = (juryIdx: number) => {
    setJury((prev) => {
      const arr = [...prev];
      if (!arr[juryIdx]) {
        // Inicializa o objeto da sessão se não existir
        arr[juryIdx] = { matchDayId: '', sessionId: '', juryMembers: [] };
      }
      arr[juryIdx].juryMembers = [...(arr[juryIdx].juryMembers || []), { position: '', name: '', category: '' }];
      return arr;
    });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!eventData) return;
    // Mapear todas as sessões do backend para facilitar busca
    const backendSessions = (eventData.matchDaySessions || []).flatMap((md: any) =>
      (md.sessions || []).map((sess: any) => ({
        ...sess,
        matchDate: md.matchDate,
        matchDayId: md.id // garantir que temos o matchDayId
      }))
    );
    // Função utilitária para converter data YYYY-MM-DD para DD/MM/YYYY
    function formatDateToDDMMYYYY(dateStr: string) {
      if (!dateStr) return '';
      const [year, month, day] = dateStr.split("-");
      return `${day}/${month}/${year}`;
    }
    // Função utilitária para garantir formato HH:mm
    function formatTimeToHHMM(timeStr: string) {
      if (!timeStr) return "";
      if (/^\d{2}:\d{2}$/.test(timeStr)) return timeStr;
      if (/^\d{2}:\d{2}:\d{2}$/.test(timeStr)) return timeStr.slice(0, 5);
      return timeStr.slice(0, 5);
    }
    // Função utilitária para calcular duração em minutos, suportando sessões que atravessam a meia-noite
    function calculateDurationMinutes(start: string, end: string): number | undefined {
      if (!start || !end) return undefined;
      const [h1, m1] = start.split(":").map(Number);
      const [h2, m2] = end.split(":").map(Number);
      let startMinutes = h1 * 60 + m1;
      let endMinutes = h2 * 60 + m2;
      // Se end <= start, passou da meia-noite
      if (endMinutes <= startMinutes) {
        endMinutes += 24 * 60;
      }
      const duration = endMinutes - startMinutes;
      return duration > 0 ? duration : undefined;
    }
    // Montar sessions para o coverSheet
    const sessions = form.sessoes.map((s: any) => {
      const backendSession = backendSessions.find((bs: any) =>
        bs.matchDate === s.date &&
        (bs.startTime === s.startTime || bs.startTime?.slice(0,5) === s.startTime?.slice(0,5))
      );
      return {
        sessionId: backendSession?.id,
        date: formatDateToDDMMYYYY(s.date),
        startTime: formatTimeToHHMM(s.startTime),
        endTime: formatTimeToHHMM(s.endTime),
        durationMinutes: calculateDurationMinutes(formatTimeToHHMM(s.startTime), formatTimeToHHMM(s.endTime))
      };
    });
    // Montar refereeEvaluations com functionBySession usando sessionId real
    const refereeEvaluations = form.avaliacoes.map((a: any) => {
      let functionBySession: Record<string, string> = {};
      if (a.functionBySession && sessions.length > 0) {
        sessions.forEach((sess: any, idx: any) => {
          const sessionId = sess.sessionId;
          if (sessionId && a.functionBySession[idx + 1]) {
            functionBySession[sessionId] = a.functionBySession[idx + 1];
          }
        });
      }
      return {
        name: a.name,
        category: a.category,
        grade: a.grade ? Number(a.grade) : undefined,
        notes: a.notes,
        functionBySession
      };
    });
    // Montar jury no formato correto
    const juryOut = jury.map(j => ({
      matchDayId: j.matchDayId,
      sessionId: j.sessionId,
      juryMembers: (j.juryMembers || []).map((m: any) => ({
        position: m.position,
        name: m.name,
        category: m.category
      }))
    }));
    const body = {
      reportType: "JA_REPORT",
      competitionId: eventData.matchDaySessions?.[0]?.competitionId,
      coverSheet: {
        style: form.estiloProva,
        councilName: eventData.association,
        sportsSeason: form.epoca,
        authorName: form.nomeAutor,
        location: form.localizacao,
        year: form.data ? new Date(form.data).getFullYear() : '',
        month: form.data ? new Date(form.data).getMonth() + 1 : '',
        numMatchDays: form.numeroJornadas,
        numSessions: form.numeroSessoes,
        sessions
      },
      register: {
        Resumo: form.resumo,
        Observações: form.observacoes
      },
      refereeEvaluations,
      jury: [] as any[] // garantir que vai sempre vazio e sem erro de tipo
    };
    const token = getCookie("token");
    const response = await fetch('/arbnet/reports/create', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `bearer ${getCookie("token")}`,
      },
      body: JSON.stringify(body)
    });
    if (!response.ok) {
      let errorMsg = 'Erro ao enviar relatório.';
      try {
        const errorData = await response.json();
        if (errorData && (errorData.detail || errorData.title)) {
          errorMsg = `${errorData.title ? errorData.title + ': ' : ''}${errorData.detail || ''}`;
        } else if (typeof errorData === 'string') {
          errorMsg = errorData;
        }
      } catch {}
      alert(errorMsg);
      return;
    }
    alert('Relatório enviado!');
  };

  if (loading) return <div>Carregando...</div>;
  if (!eventData) return <div>Erro ao carregar dados da convocatória.</div>;

  return (
    <div style={{ maxWidth: 800, margin: '2rem auto', background: '#fff', borderRadius: 8, boxShadow: '0 2px 8px #0001', padding: '2rem' }}>
      <h2>Criar Relatório do Juiz Árbitro</h2>
      <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1.2rem' }}>
        <label>
          Estilo da prova:
          <input name="estiloProva" value={form.estiloProva} onChange={handleChange} required />
        </label>
        <label>
          Época:
          <input name="epoca" value={form.epoca} onChange={handleChange} required />
        </label>
        <label>
          Nome da competição:
          <input name="nomeCompeticao" value={form.nomeCompeticao} readOnly />
        </label>
        <label>
          Nome do autor:
          <input name="nomeAutor" value={form.nomeAutor} onChange={handleChange} required />
        </label>
        <label>
          Localização:
          <input name="localizacao" value={form.localizacao} onChange={handleChange} />
        </label>
        <label>
          Data:
          <input name="data" type="date" value={form.data} onChange={handleChange} />
        </label>
        <label>
          Número de jornadas:
          <input name="numeroJornadas" type="number" value={form.numeroJornadas} onChange={handleChange} />
        </label>
        <label>
          Número de sessões:
          <input name="numeroSessoes" type="number" value={form.numeroSessoes} onChange={handleChange} />
        </label>
        <label>
          Registo/Resumo:
          <textarea name="resumo" value={form.resumo} onChange={handleChange} rows={2} />
        </label>
        <label>
          Observações:
          <textarea name="observacoes" value={form.observacoes} onChange={handleChange} rows={2} />
        </label>
        <div>
          <strong>Avaliações dos árbitros:</strong>
          <table style={{ width: '100%', marginTop: 8, borderCollapse: 'collapse', border: '1px solid #ccc', background: '#fafbfc' }}>
            <thead>
              <tr style={{ background: '#f5f5f5' }}>
                <th style={{ border: '1px solid #ccc', padding: 6 }}>Nome</th>
                <th style={{ border: '1px solid #ccc', padding: 6 }}>Categoria</th>
                <th style={{ border: '1px solid #ccc', padding: 6 }}>Avaliação (1-5)</th>
                <th style={{ border: '1px solid #ccc', padding: 6 }}>Notas</th>
                <th style={{ border: '1px solid #ccc', padding: 6 }}>Posição por Sessão</th>
              </tr>
            </thead>
            <tbody>
              {form.avaliacoes.map((a: any, idx: number) => (
                <tr key={idx}>
                  <td style={{ border: '1px solid #ccc', padding: 6 }}>{a.name}</td>
                  <td style={{ border: '1px solid #ccc', padding: 6 }}>{a.category}</td>
                  <td style={{ border: '1px solid #ccc', padding: 6 }}><input type="number" min={1} max={5} value={a.grade} onChange={e => handleAvaliacaoChange(idx, 'grade', e.target.value)} style={{ width: 60 }} /></td>
                  <td style={{ border: '1px solid #ccc', padding: 6 }}><input type="text" value={a.notes} onChange={e => handleAvaliacaoChange(idx, 'notes', e.target.value)} style={{ width: '100%' }} /></td>
                  <td style={{ border: '1px solid #ccc', padding: 6 }}>
                    {form.sessoes.map((s: any, sidx: number) => (
                      <div key={sidx} style={{ marginBottom: 4 }}>
                        Sessão {sidx + 1}: 
                        <select
                          value={a.functionBySession?.[sidx + 1] || ''}
                          onChange={e => handleFunctionBySessionChange(idx, String(sidx + 1), e.target.value)}
                          style={{ width: 160 }}
                        >
                          <option value="">Selecione Posição</option>
                          {positionOptions.map(pos => (
                            <option key={pos.id} value={pos.name}>{pos.name}</option>
                          ))}
                        </select>
                      </div>
                    ))}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        <div>
          <strong>Sessões:</strong>
          <table style={{ width: '100%', marginTop: 8, borderCollapse: 'collapse', border: '1px solid #ccc', background: '#fafbfc' }}>
            <thead>
              <tr style={{ background: '#f5f5f5' }}>
                <th style={{ border: '1px solid #ccc', padding: 6 }}>Data</th>
                <th style={{ border: '1px solid #ccc', padding: 6 }}>Início</th>
                <th style={{ border: '1px solid #ccc', padding: 6 }}>Fim</th>
                <th style={{ border: '1px solid #ccc', padding: 6 }}>Duração (min)</th>
              </tr>
            </thead>
            <tbody>
              {form.sessoes.map((s: any, idx: number) => {
                let inicio = s.startTime;
                if (inicio && inicio.length > 5) {
                  try {
                    const d = new Date(`1970-01-01T${inicio}`);
                    inicio = d.toLocaleTimeString('pt-PT', { hour: '2-digit', minute: '2-digit' });
                  } catch {}
                }
                let duracao = '';
                if (s.endTime && s.startTime) {
                  try {
                    const [h1, m1] = s.startTime.split(":").map(Number);
                    const [h2, m2] = s.endTime.split(":").map(Number);
                    duracao = ((h2 * 60 + m2) - (h1 * 60 + m1)).toString();
                  } catch {}
                }
                return (
                  <tr key={idx}>
                    <td style={{ border: '1px solid #ccc', padding: 6 }}>{s.date}</td>
                    <td style={{ border: '1px solid #ccc', padding: 6 }}>{inicio}</td>
                    <td style={{ border: '1px solid #ccc', padding: 6 }}><input type="time" value={s.endTime} onChange={e => handleSessaoChange(idx, 'endTime', e.target.value)} /></td>
                    <td style={{ border: '1px solid #ccc', padding: 6 }}>{duracao}</td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
        <div style={{ display: 'flex', gap: '1rem', marginTop: '1.5rem' }}>
          <button type="submit" style={{ background: '#1976d2', color: 'white', border: 'none', borderRadius: 4, padding: '0.7rem 1.5rem', fontWeight: 500, fontSize: 16, cursor: 'pointer' }}>
            Guardar Relatório
          </button>
        </div>
      </form>
    </div>
  );
} 