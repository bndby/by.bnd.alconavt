import { expect, test } from 'vitest'
import { nachatZahod } from '../src/zahod'

const guinness = 'Guinness Draught'
const pilsner = 'Pilsner Urquell'

test('на старте порции стоят снизу вверх, выбора нет, ходов ноль, плашки нет, запас полон', () => {
  const zahod = nachatZahod({
    vmestimost: 3,
    butylki: [
      [guinness, pilsner, guinness],
      [pilsner],
      [],
    ],
  })

  expect(zahod.snimok()).toEqual({
    vmestimost: 3,
    butylki: [
      { porciiSnizuVverh: [guinness, pilsner, guinness] },
      { porciiSnizuVverh: [pilsner] },
      { porciiSnizuVverh: [] },
    ],
    vybrannayaButylka: null,
    hody: 0,
    podpisHody: 'ходы',
    plashkaPobedy: null,
    zapas: 120,
    potolok: 120,
  })
})

test('первый тап выбирает непустую бутылку и не сдвигает порции, повторный снимает выбор', () => {
  const zahod = nachatZahod({
    vmestimost: 3,
    butylki: [
      [guinness, pilsner, guinness],
      [],
    ],
  })

  zahod.nazhat(0)

  expect(zahod.snimok().vybrannayaButylka).toBe(0)
  expect(zahod.snimok().butylki[0]?.porciiSnizuVverh).toEqual([
    guinness,
    pilsner,
    guinness,
  ])
  expect(zahod.snimok().hody).toBe(0)

  zahod.nazhat(0)

  expect(zahod.snimok().vybrannayaButylka).toBeNull()
  expect(zahod.snimok().butylki[0]?.porciiSnizuVverh).toEqual([
    guinness,
    pilsner,
    guinness,
  ])
})

test('тап по пустой бутылке без выбора ничего не выбирает', () => {
  const zahod = nachatZahod({
    vmestimost: 3,
    butylki: [[guinness], []],
  })

  zahod.nazhat(1)

  expect(zahod.snimok().vybrannayaButylka).toBeNull()
  expect(zahod.snimok().hody).toBe(0)
  expect(zahod.snimok().butylki.map((butylka) => [...butylka.porciiSnizuVverh])).toEqual([
    [guinness],
    [],
  ])
})

test('второй тап переливает верхнюю стопку одного напитка в пустую бутылку одним ходом', () => {
  const zahod = nachatZahod({
    vmestimost: 4,
    butylki: [
      [pilsner, guinness, guinness],
      [],
    ],
  })

  zahod.nazhat(0)
  zahod.nazhat(1)

  const snimok = zahod.snimok()
  expect(snimok.butylki.map((butylka) => [...butylka.porciiSnizuVverh])).toEqual([
    [pilsner],
    [guinness, guinness],
  ])
  expect(snimok.hody).toBe(1)
  expect(snimok.vybrannayaButylka).toBeNull()
  expect(snimok.plashkaPobedy).toBeNull()
})

test('из трёх верхних порций при двух свободных местах уходят две, и это один ход', () => {
  const aperol = 'Aperol'
  const zahod = nachatZahod({
    vmestimost: 6,
    butylki: [
      [aperol, guinness, guinness, guinness],
      [guinness, guinness, guinness, guinness],
    ],
  })

  zahod.nazhat(0)
  zahod.nazhat(1)

  const snimok = zahod.snimok()
  expect(snimok.butylki.map((butylka) => [...butylka.porciiSnizuVverh])).toEqual([
    [aperol, guinness],
    [guinness, guinness, guinness, guinness, guinness, guinness],
  ])
  expect(snimok.hody).toBe(1)
  expect(snimok.vybrannayaButylka).toBeNull()
})

test('недопустимый второй тап не меняет расстановку и счётчик и оставляет выбор', () => {
  const zahod = nachatZahod({
    vmestimost: 3,
    butylki: [
      [guinness, guinness],
      [pilsner, pilsner, pilsner],
      [],
    ],
  })

  zahod.nazhat(0)
  zahod.nazhat(1)

  let snimok = zahod.snimok()
  expect(snimok.butylki.map((butylka) => [...butylka.porciiSnizuVverh])).toEqual([
    [guinness, guinness],
    [pilsner, pilsner, pilsner],
    [],
  ])
  expect(snimok.hody).toBe(0)
  expect(snimok.vybrannayaButylka).toBe(0)

  zahod.nazhat(2)

  snimok = zahod.snimok()
  expect(snimok.butylki.map((butylka) => [...butylka.porciiSnizuVverh])).toEqual([
    [],
    [pilsner, pilsner, pilsner],
    [guinness, guinness],
  ])
  expect(snimok.hody).toBe(1)
  expect(snimok.vybrannayaButylka).toBeNull()
})

test('победа, когда каждая бутылка пуста или заполнена одним напитком, и плашка показывает число ходов', () => {
  const zahod = nachatZahod({
    vmestimost: 2,
    butylki: [
      [guinness],
      [guinness],
      [pilsner],
      [pilsner],
    ],
  })

  zahod.nazhat(1)
  zahod.nazhat(0)
  expect(zahod.snimok().plashkaPobedy).toBeNull()

  zahod.nazhat(3)
  zahod.nazhat(2)

  const snimok = zahod.snimok()
  expect(snimok.butylki.map((butylka) => [...butylka.porciiSnizuVverh])).toEqual([
    [guinness, guinness],
    [],
    [pilsner, pilsner],
    [],
  ])
  expect(snimok.hody).toBe(2)
  expect(snimok.plashkaPobedy).toEqual({ zagolovok: 'Победа', hody: 2 })
  expect(snimok.podpisHody).toBe('ходы')
  expect(snimok.zapas).toBe(120)
  expect(snimok.potolok).toBe(120)

  zahod.nazhat(0)
  zahod.nazhat(1)
  expect(zahod.snimok().plashkaPobedy).toEqual({ zagolovok: 'Победа', hody: 2 })
  expect(zahod.snimok().butylki[0]?.porciiSnizuVverh).toEqual([guinness, guinness])
})

test('портретный прилавок из четырёх бутылок доходит до победы за семь ходов', () => {
  const zahod = nachatZahod({
    vmestimost: 3,
    butylki: [
      [guinness, pilsner, guinness],
      [pilsner, guinness, pilsner],
      [],
      [],
    ],
  })

  const hody: readonly (readonly [number, number])[] = [
    [0, 2],
    [0, 3],
    [0, 2],
    [1, 0],
    [1, 2],
    [1, 3],
    [0, 3],
  ]
  for (const [istochnik, priemnik] of hody) {
    zahod.nazhat(istochnik)
    zahod.nazhat(priemnik)
  }

  const snimok = zahod.snimok()
  expect(snimok.plashkaPobedy).toEqual({ zagolovok: 'Победа', hody: 7 })
  expect(snimok.zapas).toBe(120)
  expect(snimok.butylki.map((butylka) => [...butylka.porciiSnizuVverh])).toEqual([
    [],
    [],
    [guinness, guinness, guinness],
    [pilsner, pilsner, pilsner],
  ])
})
