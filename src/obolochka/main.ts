import { nachatZahod } from '../zahod'
import './prilavok.css'

const guinness = 'Guinness Draught'
const pilsner = 'Pilsner Urquell'

const cvetPorcii: Record<string, string> = {
  'Guinness Draught': '#6e1b32',
  'Pilsner Urquell': '#e6c15a',
  Aperol: '#ff6a1a',
  'Giffard Crème de Violette': '#7a3cff',
  'Baileys Original Irish Cream': '#e7d3b0',
  'Giffard Lichi-Li': '#f3b6c8',
  'Bols Blue Curaçao': '#1f5bff',
  Campari: '#e23b3b',
  'Chartreuse Verte': '#b6d34a',
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

function el<K extends keyof HTMLElementTagNameMap>(
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
      mesta.push(el('span', { class: 'mesto', 'data-mesto': 'svobodno' }))
    }
    for (let i = butylka.porciiSnizuVverh.length - 1; i >= 0; i -= 1) {
      const napitok = butylka.porciiSnizuVverh[i] ?? ''
      const porciya = el('span', {
        class: 'porciya',
        'data-napitok': napitok,
      })
      porciya.style.background = cvetPorcii[napitok] ?? '#f6f1e8'
      mesta.push(porciya)
    }
    const steklo = el('span', { class: 'steklo' }, mesta)
    const knopka = el(
      'button',
      {
        class: 'butylka',
        type: 'button',
        'data-butylka': String(nomer),
        'data-vybrana': snimok.vybrannayaButylka === nomer ? 'da' : 'net',
        'aria-label': `Бутылка ${nomer + 1}`,
      },
      [el('span', { class: 'gorlyshko' }), steklo],
    )
    knopka.addEventListener('click', () => {
      zahod.nazhat(nomer)
      narisovat()
    })
    return knopka
  })

  const chasti: Node[] = [
    el('div', { class: 'polosa' }, [
      el('p', { class: 'zapas', 'data-zapas': `${snimok.zapas}/${snimok.potolok}` }, [
        el('b', {}, [document.createTextNode(`${snimok.zapas}/${snimok.potolok}`)]),
        el('span', {}, [document.createTextNode(snimok.sostoyanie)]),
      ]),
    ]),
    el('div', { class: 'ryad' }, butylki),
    el('div', { class: 'niz' }, [
      el('p', { class: 'hody', 'data-hody': String(snimok.hody) }, [
        el('b', {}, [document.createTextNode(String(snimok.hody))]),
        el('small', {}, [document.createTextNode(snimok.podpisHody)]),
      ]),
    ]),
  ]

  if (snimok.plashkaPobedy) {
    chasti.push(
      el('div', { class: 'plashka', 'data-plashka': 'pobeda' }, [
        el('h1', {}, [document.createTextNode(snimok.plashkaPobedy.zagolovok)]),
        el('p', {}, [
          el('b', {}, [document.createTextNode(String(snimok.plashkaPobedy.hody))]),
          el('small', {}, [document.createTextNode(snimok.podpisHody)]),
        ]),
      ]),
    )
  }

  koren.replaceChildren(...chasti)
}

narisovat()
