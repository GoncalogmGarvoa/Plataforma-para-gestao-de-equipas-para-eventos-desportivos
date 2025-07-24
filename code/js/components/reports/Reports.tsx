import React, { useState } from "react";
import { useCurrentUser } from "../../src/context/Authn";
import { useCurrentEmail } from "../../src/context/Referee";
import { getCookie } from "../../src/context/Authn";
import { useNavigate } from "react-router-dom";

const BASE_ENDPOINT = "/arbnet/callList/finalJuryFunction";
const PAYMENT_ENDPOINT = "/arbnet/payments/callLists";

type TabType = 'Del' | 'Ja' | 'Pay' | 'Search';

export function Reports() {
  const [activeTab, setActiveTab] = useState<TabType>('Del');
  const [reports, setReports] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState<any[]>([]);
  const [selectedType, setSelectedType] = useState('JA_REPORT');
  const [selectedReport, setSelectedReport] = useState<any>(null);
  const [searchClicked, setSearchClicked] = useState(false);

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
        headers: {
          Authorization: `bearer ${getCookie("token")}`,
        },
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
          <button
              className={`reports-tab${activeTab === 'Search' ? ' active' : ''}`}
              onClick={() => setActiveTab('Search')}
          >
            Pesquisar Relatórios
          </button>
        </div>
      </div>
      {activeTab === 'Search' && (
        <div style={{ marginTop: 24 }}>
          <h3>Pesquisar Relatórios</h3>
          <div style={{ display: 'flex', gap: 8, marginBottom: 16 }}>
            <select
              value={selectedType}
              onChange={e => setSelectedType(e.target.value)}
              style={{ padding: 8, borderRadius: 4, border: '1px solid #ccc' }}
            >
              <option value="JA_REPORT">Juiz Árbitro</option>
              <option value="DEL_REPORT">Delegado</option>
              <option value="PAY_REPORT">Pagamento</option>
            </select>
            <button
                onClick={async () => {
                  setSelectedReport(null);
                  setSearchResults([]);
                  setSearchClicked(true);
                  setLoading(true);
                  setError(null);
                  try {
                    const token = getCookie("token");

                    let endpoint = `/arbnet/reports/type/${selectedType}`;
                    if (selectedType === "PAY_REPORT") {
                      endpoint = `/arbnet/payments/type/${selectedType}`;
                    }

                    const res = await fetch(endpoint, {
                      headers: {
                        Authorization: `bearer ${token}`,
                      },
                    });

                    if (!res.ok) throw new Error('Não foram encontrados relatórios.');
                    const data = await res.json();
                    setSearchResults(Array.isArray(data) ? data : [data]);
                  } catch (e: any) {
                    setError(e.message);
                  } finally {
                    setLoading(false);
                  }
                }}
                style={{ padding: '8px 16px', borderRadius: 4, background: '#1976d2', color: 'white', border: 'none' }}
            >Pesquisar</button>
          </div>
          {loading && <div>Carregando...</div>}
          {error && searchClicked && <div style={{ color: 'red' }}>{error}</div>}
          {!loading && !error && searchClicked && searchResults.length === 0 && <div>Nenhum resultado encontrado.</div>}
          {!loading && !error && searchResults.length > 0 && (
            <ul style={{ listStyle: 'none', padding: 0, maxWidth: 500 }}>
              {searchResults.map((result, idx) => (
                <li key={result.id || idx} style={{ marginBottom: 8, border: '1px solid #eee', borderRadius: 4, padding: '6px 12px', display: 'flex', alignItems: 'center', width: 'fit-content', background: selectedReport && selectedReport.id === result.id ? '#f0f8ff' : '#fff' }}>
                  <span style={{ fontWeight: 600 }}>{(result.coverSheet?.councilName || result.paymentCoverSheet.competitionName)|| 'Competição sem nome'}</span>
                  <button
                    style={{ marginLeft: 8, padding: '2px 10px', borderRadius: 4, background: '#1976d2', color: 'white', border: 'none', cursor: 'pointer', fontSize: 14 }}
                    onClick={() => setSelectedReport(result)}
                  >Info</button>
                </li>
              ))}
            </ul>
          )}
          {selectedReport && (
              <div style={{
                position: 'fixed',
                top: 0,
                left: 0,
                width: '100vw',
                height: '100vh',
                background: 'rgba(0,0,0,0.25)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                zIndex: 1000
              }}
                   onClick={() => setSelectedReport(null)}
              >
                <div style={{
                  background: '#f9f9ff',
                  border: '1px solid #1976d2',
                  borderRadius: 8,
                  padding: 24,
                  minWidth: 340,
                  maxWidth: 600,
                  boxShadow: '0 2px 16px rgba(0,0,0,0.15)',
                  position: 'relative'
                }}
                     onClick={e => e.stopPropagation()}
                >
                  <button
                      style={{ position: 'absolute', top: 10, right: 10, background: '#1976d2', color: 'white', border: 'none', borderRadius: 4, padding: '2px 12px', fontWeight: 600, cursor: 'pointer' }}
                      onClick={() => setSelectedReport(null)}
                  >Fechar</button>

                  <h4 style={{ marginTop: 0 }}>Detalhes do Relatório</h4>

                  {/* Secção para relatórios de pagamento */}
                  {selectedReport.reportType === 'PAY_REPORT' && (
                      <>
                        <div style={{ marginBottom: 12 }}>
                          <b>Informações da Capa:</b><br />
                          <b>Conselho:</b> {selectedReport.paymentCoverSheet?.councilName || '-'}<br />
                          <b>Evento:</b> {selectedReport.paymentCoverSheet?.eventName || '-'}<br />
                          <b>Estabelecimento:</b> {selectedReport.paymentCoverSheet?.venue || '-'}<br />
                          <b>Data:</b> {selectedReport.paymentCoverSheet?.eventDate || '-'}<br />
                          <b>Hora:</b> {selectedReport.paymentCoverSheet?.eventTime || '-'}<br />
                          <b>Organização:</b> {selectedReport.paymentCoverSheet?.organization || '-'}<br />
                          <b>Morada:</b> {selectedReport.paymentCoverSheet?.location || '-'}<br />
                        </div>

                        <div style={{ marginBottom: 12 }}>
                          <b>Júri Árbitro:</b> {selectedReport.juryRefere || '-'}
                        </div>

                        <div style={{ marginBottom: 24 }}>
                          <b>Informações de Pagamento por Árbitro:</b>
                          <table style={{ width: '100%', borderCollapse: 'collapse', marginTop: 4 }}>
                            <thead>
                            <tr style={{ background: '#f5f5f5' }}>
                              <th style={{ border: '1px solid #ccc', padding: 4 }}>Nome</th>
                              <th style={{ border: '1px solid #ccc', padding: 4 }}>NIB</th>
                              <th style={{ border: '1px solid #ccc', padding: 4 }}>Refeições</th>
                              <th style={{ border: '1px solid #ccc', padding: 4 }}>Valor Pago</th>
                              <th style={{ border: '1px solid #ccc', padding: 4 }}>Presença</th>
                            </tr>
                            </thead>
                            <tbody>
                            {(selectedReport.paymentInfoPerReferee || []).map((ref: any, i: number) => (
                                <tr key={i}>
                                  <td style={{ border: '1px solid #ccc', padding: 4 }}>{ref.paymentRefInfo.name}</td>
                                  <td style={{ border: '1px solid #ccc', padding: 4 }}>{ref.paymentRefInfo.nib}</td>
                                  <td style={{ border: '1px solid #ccc', padding: 4 }}>{ref.paymentRefInfo.numberOfMeals}</td>
                                  <td style={{ border: '1px solid #ccc', padding: 4 }}>
                                    {ref.paymentRefInfo.payedAmount?.toFixed(2)}€
                                  </td>
                                  <td style={{ border: '1px solid #ccc', padding: 4 }}>
                                    {(ref.paymentRefInfo.sessionsPresence || []).map((s: any, j: number) => (
                                        <div key={j}>
                                          Dia {j + 1}:
                                          {s.morning && ` manhã às ${s.morningTime}`}
                                          {s.afternoon && ` / tarde às ${s.afternoonTime}`}
                                        </div>
                                    ))}
                                  </td>
                                </tr>
                            ))}
                            </tbody>
                          </table>
                        </div>

                        {/* Nova tabela: Detalhes de Pagamento */}
                        <div style={{ marginBottom: 24 }}>
                          <b>Detalhes de Pagamento:</b>
                          <table style={{ width: '100%', borderCollapse: 'collapse', marginTop: 4 }}>
                            <thead>
                            <tr style={{ background: '#f5f5f5' }}>
                              <th style={{ border: '1px solid #ccc', padding: 4 }}>Nome</th>
                              <th style={{ border: '1px solid #ccc', padding: 4 }}>Refeições</th>
                              <th style={{ border: '1px solid #ccc', padding: 4 }}>Transporte</th>
                              <th style={{ border: '1px solid #ccc', padding: 4 }}>Dias Úteis</th>
                              <th style={{ border: '1px solid #ccc', padding: 4 }}>Presença</th>
                              <th style={{ border: '1px solid #ccc', padding: 4 }}>Total</th>
                              <th style={{ border: '1px solid #ccc', padding: 4 }}>Em Falta</th>
                            </tr>
                            </thead>
                            <tbody>
                            {(selectedReport.paymentInfoPerReferee || []).map((ref: any, i: number) => (
                                <tr key={i}>
                                  <td style={{ border: '1px solid #ccc', padding: 4 }}>{ref.paymentRefInfo.name}</td>
                                  <td style={{ border: '1px solid #ccc', padding: 4 }}>{ref.paymentValues?.meals?.toFixed(2)}€</td>
                                  <td style={{ border: '1px solid #ccc', padding: 4 }}>{ref.paymentValues?.transportation?.toFixed(2)} €</td>
                                  <td style={{ border: '1px solid #ccc', padding: 4 }}>{ref.paymentValues?.weekDay?.toFixed(2)}€</td>
                                  <td style={{ border: '1px solid #ccc', padding: 4 }}>{ref.paymentValues?.presence?.toFixed(2)}€</td>
                                  <td style={{ border: '1px solid #ccc', padding: 4 }}><b>{ref.paymentValues?.totalOwed?.toFixed(2)}€</b></td>
                                  <td style={{ border: '1px solid #ccc', padding: 4 }}><b>{ref.paymentValues?.owedLeft?.toFixed(2)}€</b></td>
                                </tr>
                            ))}
                            </tbody>
                          </table>
                        </div>
                      </>
                  )}

                  {/* Secção para relatórios JA / DEL */}
                  {selectedReport.reportType !== 'PAY_REPORT' && (
                      <>
                        <div style={{ marginBottom: 12 }}>
                          <b>Tipo:</b> {selectedReport.reportType} &nbsp;
                          <b>Autor:</b> {selectedReport.coverSheet?.authorName || '-'} &nbsp;
                          <b>Ano:</b> {selectedReport.coverSheet?.year || '-'}
                        </div>
                        <div style={{ marginBottom: 12 }}>
                          <b>Competição:</b> {selectedReport.coverSheet?.councilName || '-'}<br />
                          <b>Localização:</b> {selectedReport.coverSheet?.location || '-'}<br />
                          <b>Época:</b> {selectedReport.coverSheet?.sportsSeason || '-'}
                        </div>
                        <div style={{ marginBottom: 12 }}>
                          <b>Sessões:</b>
                          <table style={{ width: '100%', borderCollapse: 'collapse', marginTop: 4, marginBottom: 8 }}>
                            <thead>
                            <tr style={{ background: '#f5f5f5' }}>
                              <th style={{ border: '1px solid #ccc', padding: 4 }}>Sessão</th>
                              <th style={{ border: '1px solid #ccc', padding: 4 }}>Data</th>
                              <th style={{ border: '1px solid #ccc', padding: 4 }}>Início</th>
                              <th style={{ border: '1px solid #ccc', padding: 4 }}>Fim</th>
                              <th style={{ border: '1px solid #ccc', padding: 4 }}>Duração (min)</th>
                            </tr>
                            </thead>
                            <tbody>
                            {(selectedReport.coverSheet?.sessions || []).map((s: any, i: number) => (
                                <tr key={i}>
                                  <td style={{ border: '1px solid #ccc', padding: 4 }}>Sessão {i + 1}</td>
                                  <td style={{ border: '1px solid #ccc', padding: 4 }}>{s.date}</td>
                                  <td style={{ border: '1px solid #ccc', padding: 4 }}>{s.startTime}</td>
                                  <td style={{ border: '1px solid #ccc', padding: 4 }}>{s.endTime}</td>
                                  <td style={{ border: '1px solid #ccc', padding: 4 }}>{s.durationMinutes}</td>
                                </tr>
                            ))}
                            </tbody>
                          </table>
                        </div>
                        <div style={{ marginBottom: 12 }}>
                          <b>Avaliações dos Árbitros:</b>
                          <table style={{ width: '100%', borderCollapse: 'collapse', marginTop: 4, marginBottom: 8 }}>
                            <thead>
                            <tr style={{ background: '#f5f5f5' }}>
                              <th style={{ border: '1px solid #ccc', padding: 4 }}>Nome</th>
                              <th style={{ border: '1px solid #ccc', padding: 4 }}>Categoria</th>
                              <th style={{ border: '1px solid #ccc', padding: 4 }}>Nota</th>
                              <th style={{ border: '1px solid #ccc', padding: 4 }}>Função por Sessão</th>
                              <th style={{ border: '1px solid #ccc', padding: 4 }}>Notas</th>
                            </tr>
                            </thead>
                            <tbody>
                            {(selectedReport.refereeEvaluations || []).map((a: any, i: number) => (
                                <tr key={i}>
                                  <td style={{ border: '1px solid #ccc', padding: 4 }}>{a.name}</td>
                                  <td style={{ border: '1px solid #ccc', padding: 4 }}>{a.category}</td>
                                  <td style={{ border: '1px solid #ccc', padding: 4 }}>{a.grade}</td>
                                  <td style={{ border: '1px solid #ccc', padding: 4 }}>
                                    {a.functionBySession ? Object.entries(a.functionBySession).map(([sessId, func]: any, idx: number) => (
                                        <div key={idx}>Sessão {Number(idx) + 1}: {func}</div>
                                    )) : '-'}
                                  </td>
                                  <td style={{ border: '1px solid #ccc', padding: 4 }}>{a.notes}</td>
                                </tr>
                            ))}
                            </tbody>
                          </table>
                        </div>
                        <div style={{ marginBottom: 12 }}>
                          <b>Registos:</b>
                          <ul style={{ margin: 0, paddingLeft: 16 }}>
                            {selectedReport.registers ? Object.entries(selectedReport.registers).map(([key, value]: any, idx: number) => (
                                <li key={idx}><b>{key}:</b> {value}</li>
                            )) : <li>-</li>}
                          </ul>
                        </div>
                      </>
                  )}
                </div>
              </div>
          )}
        </div>
      )}

      {activeTab !== 'Search' && (
        <div className="reports-list">
          {loading && <div>Carregando...</div>}
          {error && <div style={{ color: 'red' }}>{error}</div>}
          {!loading && !error && reports.length === 0 && <div>Nenhum há nenhum relatório para criar.</div>}
          {!loading && !error && reports.length > 0 && (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
              {reports.map((report, idx) => (
                <div key={idx} style={{
                  border: '1px solid #e0e0e0',
                  borderRadius: '8px',
                  boxShadow: '0 2px 8px rgba(0,0,0,0.04)',
                  padding: '1rem',
                  background: '#fff'
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
                      onClick={() => {
                        if (activeTab === 'Pay') {
                          navigate(`/payment-reports/create/${report.callListId}`, { state: { report } });
                        } else if (activeTab === 'Ja') {
                          navigate(`/reports/create/${report.callListId}`, { state: { report, tipo: 'juiz' } });
                        } else {
                          navigate(`/reports/create/${report.callListId}`, { state: { report } });
                        }
                      }}
                  >
                    Criar relatório
                  </button>
                </div>
              ))}
            </div>
          )}
        </div>
      )}
    </div>
  );
} 