import { ObjectValueSetterExtractor } from '../../../../../src';

test('ObjectValueSetterExtractor Test', async () => {
    let store = {
        name: 'Kiran',
        addresses: [
            {
                city: 'Bangalore',
                state: 'Karnataka',
                country: 'India',
            },
            {
                city: 'Kakinada',
                state: 'Andhra Pradesh',
                country: 'India',
            },
            { city: 'Beaverton', state: 'Oregon' },
        ],
        phone: {
            home: '080-23456789',
            office: '080-23456789',
            mobile: '080-23456789',
            mobile2: '503-23456789',
        },
        plain: [1, 2, 3, 4],
    };

    let extractor: ObjectValueSetterExtractor = new ObjectValueSetterExtractor(store, 'Store');

    expect(extractor.getValue('Store.name')).toStrictEqual('Kiran');

    extractor.setValue('Store.name', 'Kiran Kumar');
    store = extractor.getStore();

    expect(store.name).toStrictEqual('Kiran Kumar');

    extractor.setValue('Store.addresses[0].city', 'Bengaluru');
    expect(extractor.getValue('Store.addresses[0].city')).toStrictEqual('Bengaluru');

    extractor.setValue('Store.otherAddress[1].country', 'USA');
    expect(extractor.getValue('Store.otherAddress[1].country')).toStrictEqual('USA');

    extractor.setValue('Store.otherSingleAddress.country', 'USA');
    expect(extractor.getValue('Store.otherSingleAddress.country')).toStrictEqual('USA');

    extractor.setValue('Store.plain[0]', '123');
    expect(extractor.getValue('Store.plain')).toMatchObject(['123', 2, 3, 4]);

    extractor.setValue('Store.plain[0]', 1, false);
    expect(extractor.getValue('Store.plain')).toMatchObject(['123', 2, 3, 4]);

    extractor.setValue('Store.plain', undefined, true, true);
    expect(Object.keys(extractor.getValue('Store'))).toMatchObject(['name', 'addresses', 'phone', 'otherAddress', 'otherSingleAddress']);

    extractor.setValue('Store.plain', 'plainString', false, false);
    expect(extractor.getValue('Store.plain')).toStrictEqual('plainString');
});
