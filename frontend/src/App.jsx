import { useEffect, useState } from 'react'
import './App.css'
import {
  confirmRenewal,
  getPermit,
  getReferenceData,
  getRenewalQuote,
  searchPermits,
} from './api/permits'
import PermitView from './components/PermitView'
import RegisterView from './components/RegisterView'

const initialFilters = {
  permitNumber: '',
  holderName: '',
  hallId: '',
  purpose: '',
  status: '',
  from: '',
  to: '',
}

const initialPage = { content: [], page: 0, size: 10, totalElements: 0, totalPages: 0 }
const initialRenewal = { newEndDate: '', quote: null, error: '', loading: false, confirming: false }

function App() {
  const [filters, setFilters] = useState(initialFilters)
  const [page, setPage] = useState(initialPage)
  const [referenceData, setReferenceData] = useState({ halls: [], purposes: [], statuses: [] })
  const [selectedPermit, setSelectedPermit] = useState(null)
  const [loading, setLoading] = useState(true)
  const [referenceError, setReferenceError] = useState('')
  const [error, setError] = useState('')
  const [renewal, setRenewal] = useState(initialRenewal)

  useEffect(() => {
    loadReferenceData()
    loadPermits(initialFilters, 0)
  }, [])

  async function loadReferenceData() {
    try {
      setReferenceData(await getReferenceData())
    } catch (loadError) {
      setReferenceError(loadError.message)
    }
  }

  async function loadPermits(searchFilters = filters, nextPage = 0) {
    setLoading(true)
    setError('')
    try {
      setPage(await searchPermits(searchFilters, nextPage))
    } catch (loadError) {
      setError(loadError.message)
    } finally {
      setLoading(false)
    }
  }

  function updateFilter(event) {
    const { name, value } = event.target
    setFilters((current) => ({ ...current, [name]: value }))
  }

  function resetFilters() {
    setFilters(initialFilters)
    loadPermits(initialFilters, 0)
  }

  async function openPermit(permitNumber) {
    setError('')
    try {
      setSelectedPermit(await getPermit(permitNumber))
    } catch (loadError) {
      setError(loadError.message)
    }
  }

  async function getQuote(event) {
    event.preventDefault()
    setRenewal((current) => ({ ...current, loading: true, error: '', quote: null }))
    try {
      const quote = await getRenewalQuote(selectedPermit.permitNumber, renewal.newEndDate)
      setRenewal((current) => ({ ...current, quote }))
    } catch (quoteError) {
      setRenewal((current) => ({ ...current, error: quoteError.message }))
    } finally {
      setRenewal((current) => ({ ...current, loading: false }))
    }
  }

  async function confirmSelectedRenewal() {
    setRenewal((current) => ({ ...current, confirming: true, error: '' }))
    try {
      const updatedPermit = await confirmRenewal(selectedPermit.permitNumber, renewal.quote)
      setSelectedPermit(updatedPermit)
      setRenewal(initialRenewal)
      await loadPermits(filters, page.page)
    } catch (confirmError) {
      setRenewal((current) => ({ ...current, error: confirmError.message, confirming: false }))
    }
  }

  function returnToRegister() {
    setSelectedPermit(null)
    setRenewal(initialRenewal)
  }

  return (
    <main className="shell">
      <header className="topbar">
        <div className="brand-mark" aria-hidden="true">RC</div>
        <div><p className="eyebrow">Riverside Council</p><h1>Permit register</h1></div>
        <span className="staff-chip">Permits staff</span>
      </header>

      {selectedPermit ? (
        <PermitView
          permit={selectedPermit}
          renewal={renewal}
          setRenewal={setRenewal}
          onBack={returnToRegister}
          onQuote={getQuote}
          onConfirm={confirmSelectedRenewal}
        />
      ) : (
        <RegisterView
          filters={filters}
          page={page}
          referenceData={referenceData}
          loading={loading}
          error={error}
          referenceError={referenceError}
          onFilterChange={updateFilter}
          onSearch={() => loadPermits(filters, 0)}
          onReset={resetFilters}
          onPageChange={(nextPage) => loadPermits(filters, nextPage)}
          onOpen={openPermit}
        />
      )}
    </main>
  )
}

export default App
