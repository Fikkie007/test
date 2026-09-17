const API = import.meta.env.VITE_API_BASE_URL || '/api'

export async function getReferenceData() {
  return request(`${API}/reference-data`)
}

export async function searchPermits(filters, page) {
  const params = new URLSearchParams({ page: String(page), size: '10' })
  Object.entries(filters).forEach(([key, value]) => {
    if (value) params.set(key, value)
  })
  return request(`${API}/permits?${params}`)
}

export async function getPermit(permitNumber) {
  return request(`${API}/permits/${encodeURIComponent(permitNumber)}`)
}

export async function getRenewalQuote(permitNumber, newEndDate) {
  return request(`${API}/permits/${permitNumber}/renewal-quote`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ newEndDate }),
  })
}

export async function confirmRenewal(permitNumber, quote) {
  return request(`${API}/permits/${permitNumber}/renewals`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ newEndDate: quote.newEndDate, quoteId: quote.quoteId }),
  })
}

async function request(url, options) {
  const response = await fetch(url, options)
  if (!response.ok) throw await readError(response)
  return response.json()
}

async function readError(response) {
  try {
    const body = await response.json()
    return new Error(body.message || 'The request could not be completed.')
  } catch {
    return new Error('The request could not be completed.')
  }
}
