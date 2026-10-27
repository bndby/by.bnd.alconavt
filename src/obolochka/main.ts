import { nachatZahod } from '../zahod'
import './prilavok.css'

const guinness = 'Guinness Draught'
const pilsner = 'Pilsner Urquell'

const cvetPorcii: Record<string, string> = {
  'Guinness Draught': '#6e1b32',
  'Pilsner Urquell': '#e6c15a',
}

const zahod = nachatZahod({
  vmestimost: 3,
  butylki: [
    [guinness, pilsner, guinness],
    [pilsner, guinness, pilsner],
    [],
    [],
  ],
})

function sozdat<K extends keyof HTMLElementTagNameMap>(
  tag: K,
  attrs: Record<string, string> = {},
  children: Node[] = [],
): HTMLElementTagNameMap[K] {
  const node = document.createElement(tag)
  for (const [klyuch, znachenie] of Object.entries(attrs)) node.setAttribute(klyuch, znachenie)
  node.append(...children)
  return node
}

function narisovat() {
  const koren = document.querySelector('#prilavok')
  if (!koren) return
  const snimok = zahod.snimok()
  const butylki = snimok.butylki.map((butylka, nomer) => {
    const mesta = []
    const svobodno = snimok.vmestimost - butylka.porciiSnizuVverh.length
    for (let i = 0; i < svobodno; i += 1) {
      mesta.push(sozdat('span', { class: 'mesto', 'data-mesto': 'svobodno' }))
    }
    for (let i = butylka.porciiSnizuVverh.length - 1; i >= 0; i -= 1) {
      const napitok = butylka.porciiSnizuVverh[i] ?? ''
      const porciya = sozdat('span', {
        class: 'porciya',
        'data-napitok': napitok,
      })
      porciya.style.background = cvetPorcii[napitok] ?? '#f6f1e8'
      mesta.push(porciya)
    }
    const steklo = sozdat('span', { class: 'steklo' }, mesta)
    const knopka = sozdat(
      'button',
      {
        class: 'butylka',
        type: 'button',
        'data-butylka': String(nomer),
        'data-vybrana': snimok.vybrannayaButylka === nomer ? 'da' : 'net',
        'aria-label': `Бутылка ${nomer + 1}`,
      },
      [sozdat('span', { class: 'gorlyshko' }), steklo],
    )
    knopka.addEventListener('click', () => {
      zahod.nazhat(nomer)
      narisovat()
    })
    return knopka
  })

  const chasti: Node[] = [
    sozdat('div', { class: 'polosa' }, [
      sozdat('p', { class: 'zapas', 'data-zapas': `${snimok.zapas}/${snimok.potolok}` }, [
        sozdat('b', {}, [document.createTextNode(`${snimok.zapas}/${snimok.potolok}`)]),
      ]),
    ]),
  ]

  if (snimok.plashkaPobedy) {
    chasti.push(
      sozdat('div', { class: 'plashka', 'data-plashka': 'pobeda' }, [
        sozdat('h1', {}, [document.createTextNode(snimok.plashkaPobedy.zagolovok)]),
        sozdat('p', {}, [
          sozdat('b', {}, [document.createTextNode(String(snimok.plashkaPobedy.hody))]),
          sozdat('small', {}, [document.createTextNode(snimok.podpisHody)]),
        ]),
      ]),
    )
  }

  chasti.push(
    sozdat('div', { class: 'ryad' }, butylki),
    sozdat('div', { class: 'niz' }, [
      sozdat('p', { class: 'hody', 'data-hody': String(snimok.hody) }, [
        sozdat('b', {}, [document.createTextNode(String(snimok.hody))]),
        sozdat('small', {}, [document.createTextNode(snimok.podpisHody)]),
      ]),
    ]),
  )

  koren.replaceChildren(...chasti)
}

narisovat()
