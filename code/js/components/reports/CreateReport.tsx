import React, { useState, useEffect } from "react";
import { useLocation, useParams } from "react-router-dom";
import { useCurrentUser } from "../../src/context/Authn";
import { useCurrentRole } from "../../src/context/Referee";
import { getCookie } from "../callList/CreateCallList";

export function CreateReport() {
  const { callListId } = useParams();
  const location = useLocation();
  const currentUser = useCurrentUser() as { name?: string } | null;
  const currentRole = useCurrentRole();
  let tipoDocumento = "relatorio";
  if (location.state?.tipo === 'delegado') {
    tipoDocumento = "relatorio delegado";
  } else if (location.state?.tipo === 'juiz') {
    tipoDocumento = "relatorio juiz arbitro";
  }

  // Calcular época automaticamente
  const now = new Date();
  const anoAtual = now.getFullYear();
  const epocaPadrao = `${anoAtual}/${anoAtual + 1}`;

  // Calcular data atual no formato YYYY-MM-DD
  const hoje = new Date();
  const dataHoje = hoje.toISOString().split('T')[0];

  const [loading, setLoading] = useState(true);
  const [eventData, setEventData] = useState<any>(null);
  const [form, setForm] = useState<any>({
    tipoDocumento: tipoDocumento,
    estiloProva: "", // sempre vazio
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

  // Para relatorio juiz arbitro: campos para functionBySession e jury
  const [jury, setJury] = useState<any[]>([]);

  // Fetch full callList data
  useEffect(() => {
    const fetchEvent = async () => {
      setLoading(true);
      try {
        const token = getCookie("token");
        const res = await fetch(`/arbnet/callList/get/${callListId}`, {
          headers: token ? { token } : undefined
        });
        if (!res.ok) throw new Error("Erro ao buscar dados da convocatória");
        const data = await res.json();
        setEventData(data);
        // Preencher avaliações dos árbitros
        const avaliacoes = (data.participants || []).map((p: any) => ({
          name: p.userName || p.name,
          category: p.category,
          grade: '',
          notes: ''
        }));
        // Preencher sessões
        const sessoes = (data.matchDaySessions || []).flatMap((md: any) =>
          (md.sessions || []).map((s: any) => ({
            // sessionId removido
            date: md.matchDate,
            startTime: s.startTime || s.time || '',
            endTime: '',
            durationMinutes: ''
          }))
        );
        setForm((prev: any) => ({
          ...prev,
          estiloProva: '', // sempre em branco
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
        // erro
      } finally {
        setLoading(false);
      }
    };
    if (callListId) fetchEvent();
  }, [callListId, currentUser]);

  // Buscar nome do autor
  useEffect(() => {
    const fetchAuthor = async () => {
      try {
        const token = getCookie("token");
        const res = await fetch('/arbnet/users/me', { headers: token ? { token } : undefined });
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

  // Handlers para campos simples
  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  // Handlers para avaliações
  const handleAvaliacaoChange = (idx: number, field: string, value: string) => {
    setForm((prev: any) => {
      const avaliacoes = [...prev.avaliacoes];
      avaliacoes[idx] = { ...avaliacoes[idx], [field]: value };
      return { ...prev, avaliacoes };
    });
  };

  // Handlers para sessões
  const handleSessaoChange = (idx: number, field: string, value: string) => {
    setForm((prev: any) => {
      const sessoes = [...prev.sessoes];
      sessoes[idx] = { ...sessoes[idx], [field]: value };
      return { ...prev, sessoes };
    });
  };

  // Handlers para functionBySession
  const handleFunctionBySessionChange = (evalIdx: number, sessionId: string, value: string) => {
    setForm((prev: any) => {
      const avaliacoes = [...prev.avaliacoes];
      const fbSession = { ...(avaliacoes[evalIdx].functionBySession || {}) };
      fbSession[sessionId] = value;
      avaliacoes[evalIdx] = { ...avaliacoes[evalIdx], functionBySession: fbSession };
      return { ...prev, avaliacoes };
    });
  };
  // Handlers para jury
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
      arr[juryIdx].juryMembers = [...(arr[juryIdx].juryMembers || []), { position: '', name: '', category: '' }];
      return arr;
    });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!eventData) return;
    let body;
    if (location.state?.tipo === 'delegado') {
      body = {
        reportType: form.tipoDocumento,
        competitionId: eventData.competitionId || eventData.competition?.competitionNumber,
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
          sessions: form.sessoes.map((s: any) => ({
            ...s,
            durationMinutes: s.durationMinutes ? Number(s.durationMinutes) : undefined
          }))
        },
        register: {
          Resumo: form.resumo,
          Observações: form.observacoes
        },
        refereeEvaluations: form.avaliacoes.map((a: any) => ({
          name: a.name,
          category: a.category,
          grade: a.grade ? Number(a.grade) : undefined,
          notes: a.notes
        })),
        jury: [] as any[]
      };
    } else {
      // juiz arbitro
      body = {
        reportType: 'Jury Report',
        competitionId: eventData.competitionId || eventData.competition?.competitionNumber,
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
          sessions: form.sessoes.map((s: any) => ({
            ...s,
            durationMinutes: s.durationMinutes ? Number(s.durationMinutes) : undefined
          }))
        },
        register: {
          Resumo: form.resumo,
          Observações: form.observacoes
        },
        refereeEvaluations: form.avaliacoes.map((a: any) => ({
          name: a.name,
          category: a.category,
          grade: a.grade ? Number(a.grade) : undefined,
          notes: a.notes,
          functionBySession: a.functionBySession || {}
        })),
        jury: jury
      };
    }
    // Enviar para o backend
    const token = getCookie("token");
    await fetch('/arbnet/reports/create', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        ...(token ? { token } : {})
      },
      body: JSON.stringify(body)
    });
    alert('Relatório enviado!');
  };

  if (loading) return <div>Carregando...</div>;
  if (!eventData) return <div>Erro ao carregar dados da convocatória.</div>;

  return (
    <div style={{ maxWidth: 800, margin: '2rem auto', background: '#fff', borderRadius: 8, boxShadow: '0 2px 8px #0001', padding: '2rem' }}>
      <h2>Criar Relatório</h2>
      <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1.2rem' }}>
        {/* Campos comuns */}
        <label>
          Tipo de documento:
          <input name="tipoDocumento" value={tipoDocumento} onChange={handleChange} required disabled />
        </label>
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
        {/* Tabela de avaliações */}
        <div>
          <strong>Avaliações dos árbitros:</strong>
          <table style={{ width: '100%', marginTop: 8, borderCollapse: 'collapse', border: '1px solid #ccc', background: '#fafbfc' }}>
            <thead>
              <tr style={{ background: '#f5f5f5' }}>
                <th style={{ border: '1px solid #ccc', padding: 6 }}>Nome</th>
                <th style={{ border: '1px solid #ccc', padding: 6 }}>Categoria</th>
                <th style={{ border: '1px solid #ccc', padding: 6 }}>Avaliação (1-5)</th>
                <th style={{ border: '1px solid #ccc', padding: 6 }}>Notas</th>
                {location.state?.tipo === 'juiz' && <th style={{ border: '1px solid #ccc', padding: 6 }}>Função por Sessão</th>}
              </tr>
            </thead>
            <tbody>
              {form.avaliacoes.map((a: any, idx: number) => (
                <tr key={idx}>
                  <td style={{ border: '1px solid #ccc', padding: 6 }}>{a.name}</td>
                  <td style={{ border: '1px solid #ccc', padding: 6 }}>{a.category}</td>
                  <td style={{ border: '1px solid #ccc', padding: 6 }}><input type="number" min={1} max={5} value={a.grade} onChange={e => handleAvaliacaoChange(idx, 'grade', e.target.value)} style={{ width: 60 }} /></td>
                  <td style={{ border: '1px solid #ccc', padding: 6 }}><input type="text" value={a.notes} onChange={e => handleAvaliacaoChange(idx, 'notes', e.target.value)} style={{ width: '100%' }} /></td>
                  {location.state?.tipo === 'juiz' && (
                    <td style={{ border: '1px solid #ccc', padding: 6 }}>
                      {form.sessoes.map((s: any, sidx: number) => (
                        <div key={sidx} style={{ marginBottom: 4 }}>
                          Sessão {sidx + 1}: <input type="text" value={a.functionBySession?.[sidx + 1] || ''} onChange={e => handleFunctionBySessionChange(idx, String(sidx + 1), e.target.value)} style={{ width: 60 }} />
                        </div>
                      ))}
                    </td>
                  )}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        {/* Tabela de sessões */}
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
        {/* Tabela de júri para juiz arbitro */}
        {location.state?.tipo === 'juiz' && jury.length > 0 && (
          <div>
            {jury.map((j, jidx) => (
              <div key={jidx} style={{ border: '1px solid #ccc', borderRadius: 4, margin: '8px 0', padding: 8 }}>
                <label>MatchDayId: <input value={j.matchDayId} onChange={e => handleJuryChange(jidx, 'matchDayId', e.target.value)} style={{ width: 60 }} /></label>
                <label style={{ marginLeft: 8 }}>SessionId: <input value={j.sessionId} onChange={e => handleJuryChange(jidx, 'sessionId', e.target.value)} style={{ width: 60 }} /></label>
                <button type="button" onClick={() => addJuryMember(jidx)} style={{ marginLeft: 8 }}>Adicionar Membro</button>
                <div style={{ marginTop: 8 }}>
                  {j.juryMembers && j.juryMembers.map((m: any, midx: number) => (
                    <div key={midx} style={{ display: 'flex', gap: 8, marginBottom: 4 }}>
                      <input placeholder="Posição" value={m.position} onChange={e => handleJuryMemberChange(jidx, midx, 'position', e.target.value)} style={{ width: 90 }} />
                      <input placeholder="Nome" value={m.name} onChange={e => handleJuryMemberChange(jidx, midx, 'name', e.target.value)} style={{ width: 120 }} />
                      <input placeholder="Categoria" value={m.category} onChange={e => handleJuryMemberChange(jidx, midx, 'category', e.target.value)} style={{ width: 60 }} />
                    </div>
                  ))}
                </div>
              </div>
            ))}
          </div>
        )}
        <button type="submit" style={{ background: '#1976d2', color: 'white', border: 'none', borderRadius: 4, padding: '0.7rem 1.5rem', fontWeight: 500, fontSize: 16, cursor: 'pointer' }}>
          Guardar Relatório
        </button>
      </form>
    </div>
  );
} 