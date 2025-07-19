import React, { useState } from "react";
import { useLocation, useParams } from "react-router-dom";
import { useCurrentUser } from "../../src/context/Authn";

export function CreateReport() {
  const { callListId } = useParams();
  const location = useLocation();
  const reportData = location.state?.report || {};
  const currentUser = useCurrentUser() as { name?: string } | null;
  const matchDays: any[] = Array.isArray(reportData.matchDays) ? reportData.matchDays : [];

  const [form, setForm] = useState({
    tipoDocumento: "",
    estiloProva: "",
    epoca: "",
    nomeAutor: currentUser?.name || "",
    localizacao: reportData.location || "",
    data: matchDays[0]?.matchDate || "",
    numeroJornadas: matchDays.length || "",
    numeroSessoes: matchDays.reduce((acc: any, md: any) => acc + (md.sessions?.length || 0), 0) || "",
    resumo: "",
    avaliacoes: ""
  });

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    // Lógica de envio futura
    alert("Relatório salvo (simulado)");
  };

  return (
    <div style={{ maxWidth: 600, margin: '2rem auto', background: '#fff', borderRadius: 8, boxShadow: '0 2px 8px #0001', padding: '2rem' }}>
      <h2>Criar Relatório</h2>
      <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1.2rem' }}>
        <label>
          Tipo de documento:
          <input name="tipoDocumento" value={form.tipoDocumento} onChange={handleChange} required />
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
          Registo/Resumo/Observações:
          <textarea name="resumo" value={form.resumo} onChange={handleChange} rows={3} />
        </label>
        <label>
          Avaliações dos árbitros:
          <textarea name="avaliacoes" value={form.avaliacoes} onChange={handleChange} rows={3} />
        </label>
        <button type="submit" style={{ background: '#1976d2', color: 'white', border: 'none', borderRadius: 4, padding: '0.7rem 1.5rem', fontWeight: 500, fontSize: 16, cursor: 'pointer' }}>
          Guardar Relatório
        </button>
      </form>
    </div>
  );
} 