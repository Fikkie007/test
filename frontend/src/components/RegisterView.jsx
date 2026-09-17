import StatusBadge from './StatusBadge'
import { formatDate, formatMoney } from '../lib/formatters'

export default function RegisterView({
  filters,
  page,
  referenceData,
  loading,
  error,
  referenceError,
  onFilterChange,
  onSearch,
  onReset,
  onPageChange,
  onOpen,
}) {
  return (
    <section className="workspace">
      <div className="page-heading">
        <div>
          <p className="eyebrow">Staff workspace / Register</p>
          <h2>Find a permit</h2>
          <p className="muted">Search by any combination of details. Results show the newest permits first.</p>
        </div>
        <div className="result-count">{page.totalElements} {page.totalElements === 1 ? 'permit' : 'permits'}</div>
      </div>

      <SearchFilters
        filters={filters}
        referenceData={referenceData}
        onFilterChange={onFilterChange}
        onSearch={onSearch}
        onReset={onReset}
      />

      {referenceError && <div className="notice notice-error" role="alert">{referenceError}</div>}
      {error && <div className="notice notice-error" role="alert">{error}</div>}

      <PermitTable page={page} loading={loading} onOpen={onOpen} onPageChange={onPageChange} />
    </section>
  )
}

function SearchFilters({ filters, referenceData, onFilterChange, onSearch, onReset }) {
  return (
    <form className="filter-panel" onSubmit={(event) => { event.preventDefault(); onSearch() }}>
      <label>Permit number<input name="permitNumber" value={filters.permitNumber} onChange={onFilterChange} placeholder="P-2026-0001" /></label>
      <label>Holder name<input name="holderName" value={filters.holderName} onChange={onFilterChange} placeholder="Search by name" /></label>
      <label>Hall<select name="hallId" value={filters.hallId} onChange={onFilterChange}><option value="">All halls</option>{referenceData.halls.map((hall) => <option key={hall.id} value={hall.id}>{hall.name}</option>)}</select></label>
      <label>Purpose<select name="purpose" value={filters.purpose} onChange={onFilterChange}><option value="">All purposes</option>{referenceData.purposes.map((purpose) => <option key={purpose.value} value={purpose.value}>{purpose.name}</option>)}</select></label>
      <label>Status<select name="status" value={filters.status} onChange={onFilterChange}><option value="">All statuses</option>{referenceData.statuses.map((status) => <option key={status.value} value={status.value}>{status.name}</option>)}</select></label>
      <label>From<input name="from" type="date" value={filters.from} onChange={onFilterChange} /></label>
      <label>To<input name="to" type="date" value={filters.to} onChange={onFilterChange} /></label>
      <div className="filter-actions"><button className="button button-primary" type="submit">Search register</button><button className="button button-quiet" type="button" onClick={onReset}>Reset</button></div>
    </form>
  )
}

function PermitTable({ page, loading, onOpen, onPageChange }) {
  return (
    <div className="table-card">
      {loading ? <div className="state-panel"><span className="loader" />Loading permits...</div> : page.content.length === 0 ? <div className="state-panel"><strong>No permits found</strong><span>Try changing or clearing your search filters.</span></div> : <>
        <div className="table-wrap"><table><thead><tr><th>Permit</th><th>Holder</th><th>Hall</th><th>Purpose</th><th>Status</th><th>Dates</th><th className="align-right">Fee</th><th><span className="sr-only">Open</span></th></tr></thead><tbody>{page.content.map((permit) => <tr key={permit.permitNumber}><td><button className="permit-link" onClick={() => onOpen(permit.permitNumber)}>{permit.permitNumber}</button></td><td className="strong">{permit.holderName}</td><td>{permit.hall}</td><td>{permit.purpose}</td><td><StatusBadge status={permit.status} /></td><td><span>{formatDate(permit.startDate)}</span><span className="date-separator">to</span><span>{formatDate(permit.endDate)}</span></td><td className="align-right">{formatMoney(permit.fee)}</td><td><button className="arrow-button" onClick={() => onOpen(permit.permitNumber)} aria-label={`Open ${permit.permitNumber}`}>→</button></td></tr>)}</tbody></table></div>
        <div className="pagination"><span>Page {page.page + 1} of {Math.max(page.totalPages, 1)}</span><div><button className="button button-quiet" disabled={page.page === 0} onClick={() => onPageChange(page.page - 1)}>Previous</button><button className="button button-quiet" disabled={page.page + 1 >= page.totalPages} onClick={() => onPageChange(page.page + 1)}>Next</button></div></div>
      </>}
    </div>
  )
}
