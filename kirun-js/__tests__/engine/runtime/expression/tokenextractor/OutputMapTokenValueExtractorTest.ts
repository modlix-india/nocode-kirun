import { OutputMapTokenValueExtractor } from '../../../../../src/engine/runtime/tokenextractor/OutputMapTokenValueExtractor';

test('OutputMapTokenValueExtractor Test', () => {
    let phone: any = {
        phone1: '1234',
        phone2: '5678',
        phone3: '5678',
    };

    let address: any = {
        line1: 'Flat 202, PVR Estates',
        line2: 'Nagvara',
        city: 'Benguluru',
        pin: '560048',
        phone: phone,
    };

    let obj: any = {
        studentName: 'Kumar',
        math: 20,
        isStudent: true,
        address: address,
    };

    let output: Map<string, Map<string, Map<string, any>>> = new Map([
        [
            'step1',
            new Map([
                [
                    'output',
                    new Map([
                        ['zero', 0],
                        ['name', 'Kiran'],
                        ['obj', obj],
                    ]),
                ],
            ]),
        ],
    ]);

    var omtv = new OutputMapTokenValueExtractor(output);
    expect(omtv.getValue('Steps.step1.output.zero')).toBe(0);
    expect(omtv.getValue('Steps.step1.output.obj.address.phone.phone2')).toBe('5678');
});
