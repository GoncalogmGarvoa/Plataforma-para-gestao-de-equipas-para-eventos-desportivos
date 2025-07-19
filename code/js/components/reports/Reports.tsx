import React, { useState } from "react";
import { useCurrentUser } from "../../src/context/Authn";
import { useCurrentEmail } from "../../src/context/Referee";
import { getCookie } from "../callList/CreateCallList";
import { useNavigate } from "react-router-dom";

const BASE_ENDPOINT = "/arbnet/callList/finalJuryFunction";
const PAYMENT_ENDPOINT = "/arbnet/payments/callLists";

type TabType = 'Del' | 'Ja' | 'Pay';

export function Reports() {
  const [activeTab, setActiveTab] = useState<TabType>('Del');
  const [reports, setReports] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const currentUser = useCurrentUser();
  const currentEmail = useCurrentEmail();
  const navigate = useNavigate();

  const fetchReports = async (functionType: TabType) => {
    setLoading(true);
    setError(null);
    setReports([]);

    const token = getCookie("token");
    const headers: Record<string, string> = token ? { token } : {};

    try {
      let endpoint = '';
      if (functionType === 'Pay') {
        endpoint = PAYMENT_ENDPOINT;
      } else {
        endpoint = `${BASE_ENDPOINT}/${functionType}`;
      }

      const response = await fetch(endpoint, {
        method: 'GET',
        headers,
      });

      if (response.status === 404) {
        setReports([]);
        setError('Não foram encontrados relatórios.');
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
        <div>
        <h2>Relatórios</h2>
        <div className="reports-tabs" style={{ display: 'flex', gap: '1rem', marginBottom: '1rem' }}>
          <button
              className={`reports-tab${activeTab === 'Del' ? ' active' : ''}`}
              onClick={() => setActiveTab('Del')}
          >
            Delegado
          </button>
          <button
              className={`reports-tab${activeTab === 'Ja' ? ' active' : ''}`}
              onClick={() => setActiveTab('Ja')}
          >
            Juiz Árbitro
          </button>
          <button
              className={`reports-tab${activeTab === 'Pay' ? ' active' : ''}`}
              onClick={() => setActiveTab('Pay')}
          >
            Pagamento
          </button>
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
                    onClick={() =>
                        activeTab === 'Pay'
                            ? navigate(`/payment-reports/create/${report.callListId}`, { state: { report } })
                            : navigate(`/reports/create/${report.callListId}`, { state: { report } })
                    }
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