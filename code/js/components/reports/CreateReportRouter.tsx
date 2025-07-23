import React from "react";
import { useLocation } from "react-router-dom";
import { CreateDelegateReport } from "./CreateReport";
import { CreateJuizReport } from "./CreateReportJuiz";

export function CreateReportRouter() {
  const location = useLocation();
  if (location.state?.tipo === "juiz") {
    return <CreateJuizReport />;
  }
  // padrão: delegado
  return <CreateDelegateReport />;
} 