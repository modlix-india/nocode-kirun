import { HashData } from '../../../../src/engine/function/system/HashData';
import { FunctionExecutionParameters } from '../../../../src/engine/runtime/FunctionExecutionParameters';
import { KIRunFunctionRepository, KIRunSchemaRepository } from '../../../../src';
import { MapUtil } from '../../../../src/engine/util/MapUtil';

const hashData = new HashData();

describe('HashData', () => {
    test('should hash integer value with default algorithm', async () => {
        const fep = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository()
        ).setArguments(
            MapUtil.of(HashData.PARAMETER_DATA, 12345)
        );

        const result = (await hashData.execute(fep))
            .allResults()[0]
            .getResult()
            .get(HashData.EVENT_RESULT_NAME);

        expect(result).toBe('5994471abb01112afcc18159f6cc74b4f511b99806da59b3caf5a9c173cacfc5');
    });

    test('should hash double value with default algorithm', async () => {
        const fep = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository()
        ).setArguments(
            MapUtil.of(HashData.PARAMETER_DATA, 123.45)
        );

        const result = (await hashData.execute(fep))
            .allResults()[0]
            .getResult()
            .get(HashData.EVENT_RESULT_NAME);

        expect(result).toBe('4ebc4a141b378980461430980948a55988fbf56f85d084ac33d8a8f61b9fab88');
    });

    test('should hash simple string with default algorithm', async () => {
        const fep = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository()
        ).setArguments(
            MapUtil.of(HashData.PARAMETER_DATA, 'test string')
        );

        const result = (await hashData.execute(fep))
            .allResults()[0]
            .getResult()
            .get(HashData.EVENT_RESULT_NAME);

        expect(result).toBe('ee68a16fef8bf44a2b86b5614554b4079820f98dea14a67c3b507f59333cd591');
    });

    test('should hash object with default algorithm', async () => {
        const testObject = { name: 'Kailash', age: 23, city: 'Bengaluru' };

        const fep = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository()
        ).setArguments(
            MapUtil.of(HashData.PARAMETER_DATA, testObject)
        );

        const result = (await hashData.execute(fep))
            .allResults()[0]
            .getResult()
            .get(HashData.EVENT_RESULT_NAME);

        expect(result).toBe('99ca912ea0516d95398daa6503a1748b1e39a2f82350e8ece10f8f8355570a46');
    });

    test('should hash object with primitive level true and default algorithm', async () => {
        const testObject = { name: "Kailash", age: 23, city: 'Bengaluru' };

        const fep = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository()
        ).setArguments(
            new Map<string, any>([
                [HashData.PARAMETER_DATA, testObject],
                [HashData.PARAMETER_PRIMITIVE_LEVEL, true]
            ])
        );

        const result = (await hashData.execute(fep))
            .allResults()[0]
            .getResult()
            .get(HashData.EVENT_RESULT_NAME);

        // Expected object with hashed keys and values
        const expectedObject = {
            // "name": "Kailash"
            '6ffe58b85cad95f1e86f1afebc4ffba738ef6c5520811cb5f393f6c0234ffcb0': '587ba9b0b3222f5af3ad1c7495536e2d0356a9881f45059dcf619923e543b7cc',
            // "age": 23
            '78eb1fc4755453033fe186d682406e53543f575fa2b98887eceb8736e7d24567': '535fa30d7e25dd8a49f1536779734ec8286108d115da5045d77f3b4185d8f790',
            // "city": "Bengaluru"
            'b295162ccd7483bcf4f715b22b84d053efb490c5fd1beb6799751456d37c1ad1': 'afd27606e80dfd77bac4f51dabbb2ecf0ac061dd07b0fe58fc1cdde9a70ad3be'
        };

        expect(JSON.stringify(result)).toEqual(JSON.stringify(expectedObject));
    });

    test('should hash array with primitive level true', async () => {
        const testArray = ['test', 123, true, null];

        const fep = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository()
        ).setArguments(
            new Map<string, any>([
                [HashData.PARAMETER_DATA, testArray],
                [HashData.PARAMETER_PRIMITIVE_LEVEL, true]
            ])
        );

        const result = (await hashData.execute(fep))
            .allResults()[0]
            .getResult()
            .get(HashData.EVENT_RESULT_NAME);

        expect(JSON.stringify(result)).toEqual(JSON.stringify([
            '4d967a30111bf29f0eba01c448b375c1629b2fed01cdfcc3aed91f1b57d5dd5e',
            'a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3',
            'b5bea41b6c623f7c09f1bf24dcae58ebab3c0cdd90ad966bc43a45b44867e12b',
            'null'
        ]));
    });

    test('should hash with different algorithm (md5)', async () => {
        const fep = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository()
        ).setArguments(
            new Map<string, any>([
                [HashData.PARAMETER_DATA, 'test string'],
                [HashData.PARAMETER_ALGORITHM, 'md5']
            ])
        );

        const result = (await hashData.execute(fep))
            .allResults()[0]
            .getResult()
            .get(HashData.EVENT_RESULT_NAME);

        expect(result).toBe('520a193597c1170fc7f00c6e77df571f');
    });

    test('should handle null and undefined values', async () => {
        const fepNull = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository()
        ).setArguments(
            MapUtil.of(HashData.PARAMETER_DATA, null)
        );

        const resultNull = (await hashData.execute(fepNull))
            .allResults()[0]
            .getResult()
            .get(HashData.EVENT_RESULT_NAME);

        expect(resultNull).toBe("null");

        const fepUndefined = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository()
        ).setArguments(
            MapUtil.of(HashData.PARAMETER_DATA, undefined)
        );

        const resultUndefined = (await hashData.execute(fepUndefined))
            .allResults()[0]
            .getResult()
            .get(HashData.EVENT_RESULT_NAME);

        expect(resultUndefined).toBe("null");
    });

    test('should hash nested objects with primitive level', async () => {
        const nestedObject = {
            user: {
                name: 'John',
                contacts: {
                    email: 'john@example.com',
                    phone: '1234567890'
                }
            },
            active: true
        };

        const fep = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository()
        ).setArguments(
            new Map<string, any>([
                [HashData.PARAMETER_DATA, nestedObject],
                [HashData.PARAMETER_PRIMITIVE_LEVEL, true]
            ])
        );

        const result = (await hashData.execute(fep))
            .allResults()[0]
            .getResult()
            .get(HashData.EVENT_RESULT_NAME);

        const expectedObject = {
            "3190d261d186aeead3a8deec202737c7775af5c8d455a9e5ba958c48b5fd3f59": {
                "6ffe58b85cad95f1e86f1afebc4ffba738ef6c5520811cb5f393f6c0234ffcb0": "152f3ca566617488c2450ba9a88bdb8ef1593c860c496c39386d68cfe351df3f",
                "ffc121712a18c1513ded336647a0ccf7d369ba1927ac68c5d3272ac54535d633": {
                    "d55b99efba68c4d57a901b891ddbaa9d1d8ad6c52dd62390b00204efc3170441": "aaa07cfdec182c9ab13a25f43b0a84356bfe3ef199658fd4e15f96e5af54b665",
                    "d5511d58ae89a5136544686b7e471128789e3309c2ec9b520c217067f6abb75d": "9f191b3167af0649edeb8888946bee1c523b87aff6219fa7765ac731bca74fb1"
                }
            },
            "db0fb2bf5ebc424454c3e11b5ee8bfb43af24a52e561aa49e94143831dc6fd93": "b5bea41b6c623f7c09f1bf24dcae58ebab3c0cdd90ad966bc43a45b44867e12b"
        }

        expect(JSON.stringify(result)).toEqual(JSON.stringify(expectedObject));
    });

    test('Hash Meta Audience - Primitive Level - true', async () => {
        const hashData = new HashData();

        const nestedObject = [
            [
                "kunal",
                "jha",
                NaN,
                9922552168
            ],
            [
                "Saleem",
                NaN,
                "gssaleem22@gmail.com",
                9845367369
            ],
            [
                "Sunil",
                "Gayathri",
                NaN,
                9900511830
            ],
            [
                "Hemant",
                "Hemant",
                NaN,
                9619006766
            ],
            [
                "Hazarath",
                "Sarabu",
                "hazarath@yahoo.com",
                9848514909
            ]
        ];

        const fep = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository()
        ).setArguments(
            new Map<string, any>([
                [HashData.PARAMETER_DATA, nestedObject],
                [HashData.PARAMETER_ALGORITHM, 'sha256'],
                [HashData.PARAMETER_PRIMITIVE_LEVEL, true]
            ])
        );

        const result = (await hashData.execute(fep))
            .allResults()[0]
            .getResult()
            .get(HashData.EVENT_RESULT_NAME);

        const expectedObject = [
            [
                '5c5ec4214a4f90fc81d20698f3b58971a65f99be8f807167c0dc357dbfca6d64',
                '1a14fb90285bbdc811ec90c31535f8cbfe7a190cdea4388ffe5bfb5149373c85',
                '74234e98afe7498fb5daf1f36ac2d78acc339464f950703b8c019892f982b90b',
                'c73e1c882b48b71df3346a638b86a361d4ad58bd5119bed829c193192ea0bd12'
            ],
            [
                'b0a368eae50806c434c5dc52ea1743c033e20e28b37e95da3f515d426c412f24',
                "74234e98afe7498fb5daf1f36ac2d78acc339464f950703b8c019892f982b90b",
                "6aa7533f4df03ce2cdb94fbd3af0df01d3ad798997224b31bbd972c8f4110d98",
                "55dd6fc4584ff7971599839a58ea554e0ceabf8ea54108c2afe96b9600ad22de"
            ],
            [
                "d4ac1f574136969f501b11f8312613addf81a738bd32d318ff57b14c57074c46",
                "d77c692372de396251e5a30d31f8f0d1e729fc2d2ddf38286d4f9133aa7849de",
                "74234e98afe7498fb5daf1f36ac2d78acc339464f950703b8c019892f982b90b",
                "859aee372e6161388c67a2ebe39ea30da2f09d6e4dca715be79109d8c9c9a946"
            ],
            [
                "8ff58050b37737f3b0d5b9541c1cb1f67394b41631bfdc927ab216ff580af445",
                "8ff58050b37737f3b0d5b9541c1cb1f67394b41631bfdc927ab216ff580af445",
                "74234e98afe7498fb5daf1f36ac2d78acc339464f950703b8c019892f982b90b",
                "3aaf978a21568f3132e586b5fab896a46930831d8362ae3a630f9b0e1ac44d5a"
            ],
            [
                "ff1363632be96e64e74f8f6ce09e899dc93243a0aba24ae8d1e901df266e783b",
                "60124d4663c3581f6d5ab01d3a8652e67c83a0eea14d2eebc617132386a63533",
                "5df5828651d67e079dbaa41176ed84c87e1823a316abd2a55ade9c9045dadb0a",
                "c6701bb10611df1b1ac0f1bc3d008200a81ccb78a2e3e71db8d0e6b6ad6bca83"
            ]
        ]

        expect(JSON.stringify(result)).toEqual(JSON.stringify(expectedObject));
    });

    test('should hash 2D array with primitive level true', async () => {
        const twoDArray = [
            ['a', 1, true],
            ['b', 2, false],
            ['c', 3, null]
        ];

        const fep = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository()
        ).setArguments(
            new Map<string, any>([
                [HashData.PARAMETER_DATA, twoDArray],
                [HashData.PARAMETER_PRIMITIVE_LEVEL, true]
            ])
        );

        const result = (await hashData.execute(fep))
            .allResults()[0]
            .getResult()
            .get(HashData.EVENT_RESULT_NAME);

        const expectedArray = [
            [
                'ac8d8342bbb2362d13f0a559a3621bb407011368895164b628a54f7fc33fc43c', // hash of "a"
                '6b86b273ff34fce19d6b804eff5a3f5747ada4eaa22f1d49c01e52ddb7875b4b', // hash of 1
                'b5bea41b6c623f7c09f1bf24dcae58ebab3c0cdd90ad966bc43a45b44867e12b'  // hash of true
            ],
            [
                'c100f95c1913f9c72fc1f4ef0847e1e723ffe0bde0b36e5f36c13f81fe8c26ed', // hash of "b"
                'd4735e3a265e16eee03f59718b9b5d03019c07d8b6c51f90da3a666eec13ab35', // hash of 2
                'fcbcf165908dd18a9e49f7ff27810176db8e9f63b4352213741664245224f8aa'  // hash of false
            ],
            [
                '879923da020d1533f4d8e921ea7bac61e8ba41d3c89d17a4d14e3a89c6780d5d', // hash of "c"
                '4e07408562bedb8b60ce05c1decfe3ad16b72230967de01f640b7e4729b49fce', // hash of 3
                'null'                                                                 // null value
            ]
        ];

        expect(JSON.stringify(result)).toEqual(JSON.stringify(expectedArray));
    });
    
});