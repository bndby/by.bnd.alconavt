export type Rasstanovka = {
  vmestimost: number
  butylki: readonly (readonly string[])[]
}

export type SnimokPrilavka = {
  vmestimost: number
  butylki: readonly { porciiSnizuVverh: readonly string[] }[]
  vybrannayaButylka: number | null
  hody: number
  podpisHody: 'ходы'
  plashkaPobedy: null | { zagolovok: 'Победа'; hody: number }
  zapas: 120
  potolok: 120
  sostoyanie: 'Трезв'
}

export type Zahod = {
  nazhat(nomerButylkiSleva: number): void
  snimok(): SnimokPrilavka
}

function etoPobeda(butylki: readonly (readonly string[])[], vmestimost: number): boolean {
  return butylki.every((butylka) => {
    if (butylka.length === 0) return true
    return butylka.length === vmestimost && butylka.every((porciya) => porciya === butylka[0])
  })
}

function verhnyayaStopka(butylka: readonly string[]): { napitok: string; dlina: number } | null {
  const napitok = butylka[butylka.length - 1]
  if (napitok === undefined) return null
  let dlina = 0
  for (let i = butylka.length - 1; i >= 0 && butylka[i] === napitok; i -= 1) dlina += 1
  return { napitok, dlina }
}

export function nachatZahod(rasstanovka: Rasstanovka): Zahod {
  const butylki = rasstanovka.butylki.map((butylka) => [...butylka])
  let vybrannayaButylka: number | null = null
  let hody = 0

  return {
    nazhat(nomerButylkiSleva) {
      if (etoPobeda(butylki, rasstanovka.vmestimost)) return
      const butylka = butylki[nomerButylkiSleva]
      if (!butylka) return
      if (vybrannayaButylka === null) {
        if (butylka.length === 0) return
        vybrannayaButylka = nomerButylkiSleva
        return
      }
      if (vybrannayaButylka === nomerButylkiSleva) {
        vybrannayaButylka = null
        return
      }

      const istochnik = butylki[vybrannayaButylka]
      const stopka = istochnik ? verhnyayaStopka(istochnik) : null
      if (!istochnik || !stopka) return
      const svobodno = rasstanovka.vmestimost - butylka.length
      const totZheSverhu = butylka.length === 0 || butylka[butylka.length - 1] === stopka.napitok
      if (!totZheSverhu || svobodno <= 0) return
      const skolko = Math.min(stopka.dlina, svobodno)
      const snyatye: string[] = []
      for (let i = 0; i < skolko; i += 1) {
        const porciya = istochnik.pop()
        if (porciya !== undefined) snyatye.push(porciya)
      }
      for (let i = snyatye.length - 1; i >= 0; i -= 1) {
        const porciya = snyatye[i]
        if (porciya !== undefined) butylka.push(porciya)
      }
      hody += 1
      vybrannayaButylka = null
    },
    snimok() {
      return {
        vmestimost: rasstanovka.vmestimost,
        butylki: butylki.map((porcii) => ({ porciiSnizuVverh: [...porcii] })),
        vybrannayaButylka,
        hody,
        podpisHody: 'ходы',
        plashkaPobedy: etoPobeda(butylki, rasstanovka.vmestimost)
          ? { zagolovok: 'Победа', hody }
          : null,
        zapas: 120,
        potolok: 120,
        sostoyanie: 'Трезв',
      }
    },
  }
}
