import RenewalCard from './RenewalCard'
import StatusBadge from './StatusBadge'
import { formatDate, formatMoney } from '../lib/formatters'

export default function PermitView({ permit, renewal, setRenewal, onBack, onQuote, onConfirm }) {
  return (
    <section className="workspace">
      <button className="back-link" onClick={onBack}>← Back to register</button>
      <div className="detail-heading"><div><p className="eyebrow">Permit detail</p><h2>{permit.permitNumber}</h2><p className="muted">Read-only permit record and renewal history</p></div><StatusBadge status={permit.status} /></div>
      <div className="detail-grid">
        <PermitSummary permit={permit} />
        <RenewalCard permit={permit} renewal={renewal} setRenewal={setRenewal} onQuote={onQuote} onConfirm={onConfirm} />
      </div>
      {permit.renewalHistory.length > 0 && <RenewalHistory history={permit.renewalHistory} />}
    </section>
  )
}

function PermitSummary({ permit }) {
  return <article className="card permit-card"><div className="card-topline"><span className="card-kicker">Permit holder</span><span className="permit-number">{permit.permitNumber}</span></div><h3>{permit.holderName}</h3><dl className="details"><div><dt>Hall</dt><dd>{permit.hall.name}</dd></div><div><dt>District</dt><dd>{permit.hall.district}</dd></div><div><dt>Purpose</dt><dd>{permit.purpose}</dd></div><div><dt>Permit fee</dt><dd>{formatMoney(permit.fee)}</dd></div><div><dt>Start date</dt><dd>{formatDate(permit.startDate)}</dd></div><div><dt>End date</dt><dd>{formatDate(permit.endDate)}</dd></div></dl></article>
}

function RenewalHistory({ history }) {
  return <article className="card history-card"><div className="section-heading"><div><p className="card-kicker">Record of changes</p><h3>Renewal history</h3></div><span>{history.length} {history.length === 1 ? 'renewal' : 'renewals'}</span></div><div className="history-list">{history.map((item) => <div className="history-row" key={item.id}><div><strong>Renewed permit</strong><span>{formatDate(item.previousEndDate)} → {formatDate(item.newEndDate)}</span></div><strong>{formatMoney(item.fee)}</strong></div>)}</div></article>
}
