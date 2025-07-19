import React, { useState } from "react";
import { useCurrentUser } from "../../src/context/Authn";
import { useCurrentEmail } from "../../src/context/Referee";
import { getCookie } from "../callList/CreateCallList";
import { useNavigate } from "react-router-dom";

const BASE_ENDPOINT = "/arbnet/callList/finalJuryFunction";

export function Reports() {
  const [activeTab, setActiveTab] = useState<'Del' | 'Ja'>('Del');
  const [reports, setReports] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const currentUser = useCurrentUser();
  const currentEmail = useCurrentEmail();
  const navigate = useNavigate();

  const fetchReports = async (functionType: 'Del' | 'Ja') => {
    setLoading(true);
    setError(null);
    setReports([]);
    try {
      const token = getCookie("token");
      const endpoint = `${BASE_ENDPOINT}/${functionType}`;
      const headers: Record<string, string> = token ? { token } : {};
      const response = await fetch(endpoint, {
        method: 'GET',
        headers,
      });
      if (response.status === 404) {
        setReports([]);
        setError('Não foram encontradas convocações para fazer o relatório.');
        return;
      }
      if (!response.ok) throw new Error('Erro ao buscar relatórios');
      const data = await response.json();
      setReports(Array.isArray(data) ? data : [data]);
    } catch (e: any) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  };

  React.useEffect(() => {
    fetchReports(activeTab);
    // eslint-disable-next-line
  }, [activeTab]);

  return (
    <div>
      <h2>Relatórios</h2>
      <div className="reports-tabs">
        <div
          className={`reports-tab${activeTab === 'Del' ? ' active' : ''}`}
          onClick={() => setActiveTab('Del')}
        >
          Relatórios de Delegado
        </div>
        <div
          className={`reports-tab${activeTab === 'Ja' ? ' active' : ''}`}
          onClick={() => setActiveTab('Ja')}
        >
          Relatórios de Juiz Árbitro
        </div>
      </div>
      <div className="reports-list">
        {loading && <div>Carregando...</div>}
        {error && <div style={{ color: 'red' }}>{error}</div>}
        {!loading && !error && reports.length === 0 && <div>Nenhum relatório encontrado.</div>}
        {!loading && !error && reports.length > 0 && (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
            {reports.map((report, idx) => (
              <div key={idx} style={{
                border: '1px solid #e0e0e0',
                borderRadius: '8px',
                boxShadow: '0 2px 8px rgba(0,0,0,0.04)',
                padding: '1.2rem 1.5rem',
                background: '#fafbfc',
                maxWidth: 500
              }}>
                <div style={{ fontSize: '1.1rem', fontWeight: 600, marginBottom: 6 }}>
                  Nome: {report.competitionName}
                </div>
                <div style={{ color: '#555', marginBottom: 8 }}>
                  <span style={{ fontWeight: 500 }}>Nº Convocatória:</span> {report.callListId}
                </div>
                <div style={{ fontWeight: 500, marginBottom: 4 }}>Dias:</div>
                <ul style={{ paddingLeft: 20, margin: 0 }}>
                  {Array.isArray(report.matchDays) && report.matchDays.length > 0 ? (
                    report.matchDays.map((md: any, i: number) => {
                      // Formatar data para DD/MM/YYYY
                      let dateStr = md.matchDate;
                      try {
                        const d = new Date(md.matchDate);
                        if (!isNaN(d.getTime())) {
                          dateStr = d.toLocaleDateString('pt-PT');
                        }
                      } catch {}
                      return (
                        <li key={i} style={{ display: 'flex', alignItems: 'center', gap: 6, color: '#333' }}>
                          <span role="img" aria-label="Calendário">📅</span> {dateStr}
                        </li>
                      );
                    })
                  ) : (
                    <li style={{ color: '#888' }}>Nenhum dia encontrado</li>
                  )}
                </ul>
                <button style={{
                  marginTop: '1rem',
                  padding: '0.5rem 1.2rem',
                  background: '#1976d2',
                  color: 'white',
                  border: 'none',
                  borderRadius: '4px',
                  cursor: 'pointer',
                  fontWeight: 500
                }}
                  onClick={() => navigate(`/reports/create/${report.callListId}`, { state: { report } })}
                >
                  Criar relatório
                </button>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
} 