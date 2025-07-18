import React, { useState } from "react";
import { useCurrentUser } from "../../src/context/Authn";
import { useCurrentEmail } from "../../src/context/Referee";
import { getCookie } from "../callList/CreateCallList";

const BASE_ENDPOINT = "/arbnet/callList/finalJuryFunction";

export function Reports() {
  const [activeTab, setActiveTab] = useState<'Del' | 'Ja'>('Del');
  const [reports, setReports] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const currentUser = useCurrentUser();
  const currentEmail = useCurrentEmail();

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
          <ul>
            {reports.map((report, idx) => (
              <li key={idx}>{JSON.stringify(report)}</li>
            ))}
          </ul>
        )}
      </div>
    </div>
  );
} 